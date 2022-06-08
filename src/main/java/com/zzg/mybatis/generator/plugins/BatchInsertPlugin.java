package com.zzg.mybatis.generator.plugins;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.zzg.mybatis.generator.util.MyStringUtils;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * ---------------------------------------------------------------------------
 * 批量插入插件
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPlugin extends PluginAdapter {
        public static final String METHOD_BATCH_INSERT = "insertBatch"; // 方法名

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean validate(List<String> warnings) {
                return true;
        }

        /**
        * Java Client Methods 生成
        * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
        * @param interfaze
        * @param topLevelClass
        * @param introspectedTable
        * @return
        */
        @Override
        public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
                        IntrospectedTable introspectedTable) {
                addBatchInsertMethod(interfaze, introspectedTable);
                return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
        }

        private void addBatchInsertMethod(Interface interfaze,
                        IntrospectedTable introspectedTable) {
                // 设置需要导入的类
                Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
                importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
                importedTypes.add(
                                new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

                Method ibsmethod = new Method();
                // 1.设置方法可见性
                ibsmethod.setVisibility(JavaVisibility.PUBLIC);
                // 2.设置返回值类型
                FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();// int型
                ibsmethod.setReturnType(ibsreturnType);
                // 3.设置方法名
                ibsmethod.setName(METHOD_BATCH_INSERT);
                // 4.设置参数列表
                FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
                FullyQualifiedJavaType paramListType;
                if (introspectedTable.getRules().generateBaseRecordClass()) {
                        paramListType = new FullyQualifiedJavaType(
                                        introspectedTable.getBaseRecordType());
                } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                        paramListType = new FullyQualifiedJavaType(
                                        introspectedTable.getPrimaryKeyType());
                } else {
                        throw new RuntimeException(getString("RuntimeError.12")); //$NON-NLS-1$
                }
                paramType.addTypeArgument(paramListType);

                ibsmethod.addParameter(new Parameter(paramType, "list"));

                interfaze.addImportedTypes(importedTypes);
                interfaze.addMethod(ibsmethod);

        }

        /**
        * SQL Map Methods 生成
        * 具体执行顺序 http://www.mybatis.org/generator/reference/pluggingIn.html
        * @param document
        * @param introspectedTable
        * @return
        */
        @Override
        public boolean sqlMapDocumentGenerated(Document document,
                        IntrospectedTable introspectedTable) {
                addBatchInsertXml(document, introspectedTable);
                return super.sqlMapDocumentGenerated(document, introspectedTable);

        }

        private void addBatchInsertXml(Document document, IntrospectedTable introspectedTable) {
                List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
                String keyColumn = introspectedTable.getPrimaryKeyColumns().get(0)
                                .getActualColumnName();

                XmlElement batchInsertEle = new XmlElement("insert");
                batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
                batchInsertEle.addAttribute(new Attribute("parameterType", "java.util.List"));

                batchInsertEle.addElement(new TextElement("insert into "
                                + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()
                                + " ("));

                // 添加foreach节点
                XmlElement foreachElement = new XmlElement("foreach");
                foreachElement.addAttribute(new Attribute("collection", "list"));
                foreachElement.addAttribute(new Attribute("item", "item"));
                foreachElement.addAttribute(new Attribute("separator", ","));

                foreachElement.addElement(new TextElement("("));

                String tab = "    ";

                StringBuilder columnStr = new StringBuilder();
                StringBuilder valueStr = new StringBuilder();

                columnStr.append(tab);
                valueStr.append(tab);

                int index = 0;
                int count = 0;
                for (IntrospectedColumn introspectedColumn : columns) {
                        index++;

                        String columnName = introspectedColumn.getActualColumnName();
                        //不是自增字段的才会出现在批量插入中
                        if (!columnName.toUpperCase().equalsIgnoreCase(keyColumn)) {
                                count++;
                                columnStr.append(MyStringUtils.wrapColumnName(columnName))
                                                .append(index < columns.size() ? ", " : "");

                                valueStr.append("#{item.")
                                                .append(introspectedColumn.getJavaProperty())
                                                .append(",jdbcType=")
                                                .append(introspectedColumn.getJdbcTypeName())
                                                .append("}")
                                                .append(index < columns.size() ? ", " : "");

                        }

                        // 3个字段一行
                        if ((count > 0 && count % 3 == 0) || index == columns.size()) {
                                batchInsertEle.addElement(new TextElement(columnStr.toString()));
                                columnStr.setLength(0);
                                foreachElement.addElement(new TextElement(valueStr.toString()));
                                valueStr.setLength(0);

                                columnStr.append(tab);
                                valueStr.append(tab);
                        }
                }
                foreachElement.addElement(new TextElement(")"));

                // values 构建
                batchInsertEle.addElement(new TextElement(") values"));
                batchInsertEle.addElement(foreachElement);
                document.getRootElement().addElement(batchInsertEle);
        }



}