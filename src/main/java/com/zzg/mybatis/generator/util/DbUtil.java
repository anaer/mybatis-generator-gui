package com.zzg.mybatis.generator.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zzg.mybatis.generator.exception.DbDriverLoadingException;
import com.zzg.mybatis.generator.model.DatabaseConfig;
import com.zzg.mybatis.generator.model.DbType;
import com.zzg.mybatis.generator.model.UITableColumnVO;
import com.zzg.mybatis.generator.view.AlertUtil;
import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.convert.ConvertUtil;
import org.dromara.hutool.core.regex.ReUtil;
import org.dromara.hutool.core.text.StrUtil;
import org.dromara.hutool.core.text.split.SplitUtil;
import org.mybatis.generator.internal.util.ClassloaderUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Owen on 6/12/16.
 */
public class DbUtil {

    private static final Logger _LOG = LoggerFactory.getLogger(DbUtil.class);
    private static final int DB_CONNECTION_TIMEOUTS_SECONDS = 1;

    private static Map<DbType, Driver> drivers = new HashMap<>();

	private static ExecutorService executorService = Executors.newSingleThreadExecutor();
	private static volatile boolean portForwarding = false;
	private static Map<Integer, Session> portForwardingSession = new ConcurrentHashMap<>();

    public static Session getSSHSession(DatabaseConfig databaseConfig) {
		if (!ArrayUtil.isAllNotBlank(databaseConfig.getSshHost(), databaseConfig.getSshPort(), databaseConfig.getSshUser(), databaseConfig.getPrivateKey())
		  && StrUtil.isBlank(databaseConfig.getSshPassword()))
	    {
			return null;
		}

		Session session = null;
		try {
			//Set StrictHostKeyChecking property to no to avoid UnknownHostKey issue
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Integer sshPort = ConvertUtil.toInt(databaseConfig.getSshPort());
			int port = sshPort == null ? 22 : sshPort;
			session = jsch.getSession(databaseConfig.getSshUser(), databaseConfig.getSshHost(), port);
			if (StrUtil.isNotBlank(databaseConfig.getPrivateKey())) {
				//使用秘钥方式认证
				jsch.addIdentity(databaseConfig.getPrivateKey(), databaseConfig.getPrivateKeyPassword());
			}else {
				session.setPassword(databaseConfig.getSshPassword());
			}
			session.setConfig(config);
		}catch (JSchException e) {
			//Ignore
		}
		return session;
	}

	public static void engagePortForwarding(Session sshSession, DatabaseConfig config) {
		if (sshSession != null) {
			AtomicInteger assigned_port = new AtomicInteger();
			Future<?> result = executorService.submit(() -> {
				try {
					Integer localPort = ConvertUtil.toInt(config.getLport());
					Integer RemotePort = ConvertUtil.toInt(config.getRport());
					int lport = localPort == null ? Integer.parseInt(config.getPort()) : localPort;
					int rport = RemotePort == null ? Integer.parseInt(config.getPort()) : RemotePort;
					Session session = portForwardingSession.get(lport);
					if (session != null && session.isConnected()) {
						String s = session.getPortForwardingL()[0];
						String[] split = SplitUtil.splitToArray(s, ":");
						boolean portForwarding = (lport + ":" + config.getHost()).equals(String.format("%s:%s", split[0], split[1]));
						if (portForwarding) {
							return;
						}
					}
					sshSession.connect();
					assigned_port.set(sshSession.setPortForwardingL(lport, config.getHost(), rport));
					portForwardingSession.put(lport, sshSession);
					portForwarding = true;
					_LOG.info("portForwarding Enabled, {}", assigned_port);
				} catch (JSchException e) {
					_LOG.error("Connect Over SSH failed", e);
					if (e.getCause() != null && "Address already in use: JVM_Bind".equals(e.getCause().getMessage())) {
						throw new RuntimeException("Address already in use: JVM_Bind");
					}
					throw new RuntimeException(e.getMessage());
				}
			});
			try {
				result.get(5, TimeUnit.SECONDS);
			}catch (Exception e) {
				shutdownPortForwarding(sshSession);
				if (e.getCause() instanceof RuntimeException) {
					throw (RuntimeException)e.getCause();
				}
				if (e instanceof TimeoutException) {
					throw new RuntimeException("OverSSH 连接超时：超过5秒");
				}

				_LOG.info("executorService isShutdown:{}", executorService.isShutdown());
				AlertUtil.showErrorAlert("OverSSH 失败，请检查连接设置:" + e.getMessage());
			}
		}
	}

	public static void shutdownPortForwarding(Session session) {
		portForwarding = false;
		if (session != null && session.isConnected()) {
			session.disconnect();
			_LOG.info("portForwarding turn OFF");
		}
//		executorService.shutdown();
	}

    public static Connection getConnection(DatabaseConfig config) throws ClassNotFoundException, SQLException {
		DbType dbType = DbType.valueOf(config.getDbType());
		if (drivers.get(dbType) == null){
			loadDbDriver(dbType);
		}

		String url = getConnectionUrlWithSchema(config);
	    Properties props = new Properties();

	    props.setProperty("user", config.getUsername()); //$NON-NLS-1$
	    props.setProperty("password", config.getPassword()); //$NON-NLS-1$

		DriverManager.setLoginTimeout(DB_CONNECTION_TIMEOUTS_SECONDS);
	    Connection connection = drivers.get(dbType).connect(url, props);
        _LOG.info("getConnection, connection url: {}", connection);
        return connection;
    }

    public static List<String> getTableNames(DatabaseConfig config, String filter) throws Exception {
		Session sshSession = getSSHSession(config);
		engagePortForwarding(sshSession, config);
		try (Connection connection = getConnection(config)) {
			List<String> tables = new ArrayList<>();
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs;
			if (DbType.valueOf(config.getDbType()) == DbType.SQL_Server) {
				String sql = "select name from sysobjects  where xtype='u' or xtype='v' order by name";
				rs = connection.createStatement().executeQuery(sql);
				while (rs.next()) {
					tables.add(rs.getString("name"));
				}
			} else if (DbType.valueOf(config.getDbType()) == DbType.Oracle) {
				rs = md.getTables(null, config.getUsername().toUpperCase(), null, new String[]{"TABLE", "VIEW"});
			} else if (DbType.valueOf(config.getDbType()) == DbType.Sqlite) {
				String sql = "Select name from sqlite_master;";
				rs = connection.createStatement().executeQuery(sql);
				while (rs.next()) {
					tables.add(rs.getString("name"));
				}
			} else {
				// rs = md.getTables(null, config.getUsername().toUpperCase(), null, null);
				rs = md.getTables(config.getSchema(), null, "%", new String[]{"TABLE", "VIEW"});//针对 postgresql 的左侧数据表显示
			}
			while (rs.next()) {
				tables.add(rs.getString(3));
			}
			if (StrUtil.isNotBlank(filter)) {
				tables.removeIf(x -> !x.contains(filter) && !(x.replaceAll("_", "").contains(filter)));;
			}

			// 过滤带日期的备份表 如: t_user_2022-06-01
			tables.removeIf(x -> ReUtil.isMatch("^(.*)_\\d{4}[_-]?\\d{2}[_-]?\\d{2}$", x));

			if (tables.size() > 1) {
				Collections.sort(tables);
			}
			return tables;
		} finally {
			shutdownPortForwarding(sshSession);
		}
	}

    public static List<UITableColumnVO> getTableColumns(DatabaseConfig dbConfig, String tableName) throws Exception {
        String url = getConnectionUrlWithSchema(dbConfig);
        _LOG.info("getTableColumns, connection url: {}", url);
		Session sshSession = getSSHSession(dbConfig);
		engagePortForwarding(sshSession, dbConfig);
		Connection conn = getConnection(dbConfig);
		try {
			DatabaseMetaData md = conn.getMetaData();
			ResultSet rs = md.getColumns(dbConfig.getSchema(), null, tableName, null);
			List<UITableColumnVO> columns = new ArrayList<>();
			while (rs.next()) {
				UITableColumnVO columnVO = new UITableColumnVO();
				String columnName = rs.getString("COLUMN_NAME");
				columnVO.setColumnName(columnName);
				columnVO.setJdbcType(rs.getString("TYPE_NAME"));
				columns.add(columnVO);
			}
			return columns;
		} finally {
			conn.close();
			shutdownPortForwarding(sshSession);
		}
	}

    public static String getConnectionUrlWithSchema(DatabaseConfig dbConfig) throws ClassNotFoundException {
		DbType dbType = DbType.valueOf(dbConfig.getDbType());
		String connectionUrl = String.format(dbType.getConnectionUrlPattern(),
				portForwarding ? "127.0.0.1" : dbConfig.getHost(), portForwarding ? dbConfig.getLport() : dbConfig.getPort(), dbConfig.getSchema(), dbConfig.getEncoding());
        _LOG.info("getConnectionUrlWithSchema, connection url: {}", connectionUrl);
        return connectionUrl;
    }

	/**
	 * 加载数据库驱动
	 * @param dbType 数据库类型
	 */
	private static void loadDbDriver(DbType dbType){
		List<String> driverJars = ConfigHelper.getAllJDBCDriverJarPaths();
		ClassLoader classloader = ClassloaderUtility.getCustomClassloader(driverJars);
		try {
			Class<?> clazz = Class.forName(dbType.getDriverClass(), true, classloader);
			Driver driver = (Driver) clazz.newInstance();
			_LOG.info("load driver class: {}", driver);
			drivers.put(dbType, driver);
		} catch (Exception e) {
			_LOG.error("load driver error", e);
			throw new DbDriverLoadingException("找不到"+dbType.getConnectorJarFile()+"驱动");
		}
	}
}
