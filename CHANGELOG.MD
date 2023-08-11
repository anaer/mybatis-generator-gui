# CHANGELOG

## 20230811

1. 升级hutool -> 5.8.21
2. 修复warning问题
3. 如果匹配结果只有一项时, 自动设置表名和当前选中数据库配置

## 20230720

1. 升级hutool -> 5.8.20, slf4j -> 2.0.7
2. 调整DbRemarksComment注释生成, 未勾选时 注释与默认保持一致 (兼容其他生成器)

## 20230307

1. pom.xml提取version到properties
2. 修改配置弹窗的列宽, 取消自适应

## 20230130

1. 左侧连接管理栏添加右键菜单`复制连接`, 复制当前选中的连接进行编辑, 保存时新增记录.

## 20230116

1. 去除依赖commons-lang3, commons-io, commons-beanutils, 使用hutool替代
2. AlertUtil提取公共方法

## 20221213

1. 升级logback版本 -> 1.3.5, slf4j -> 2.0.6
2. 调整保存配置对话框 输入框宽度

## 20220608

1. 去除 itfsw 依赖, 自实现 BatchInsertPlugin, 主要是 enableSelective 及 ModelColumnPlugin 基本没使用到, 后续有需要再进行调整
2. 升级 mybatis-generator-core 1.3.7 -> 1.4.1, 有些兼容性问题, 已处理
   新版生成的参数名有调整 record -> row
3. 升级 log 包
4. 去除 fastjson 依赖, 因为仅有一个地方用到, 可以先试用 hutool 中的 JSONUtil 工具顶替

## 20220601

1. 调整界面布局
2. 保存配置对话框 文本输入框 宽度自动适配
3. 配置列表, 默认按照名称排序
4. 修复拼写错误
5. 表列表 过滤带日期备份表 如: t_user_2022-06-01

## 20220120

1. 相关依赖包版本升级
2. 直接添加 LombokPlugin.java
3. 应用配置, 设置过滤文本框, 并触发事件进行过滤
4. 修改主界面样式

## 20211129

1. 添加 hutool -> 5.7.16
2. `com.zzg.mybatis.generator.util.MyStringUtils` 支持配置忽略的表名前缀
   默认忽略列表: `ignoreTablePrefix = { "t_" }`
3. `com.zzg.mybatis.generator.controller.MainUIController` 支持配置 Mapper 后缀
   目前的开发规范为 Mapper 后缀
4. 调整 MainUI.fxml 首页布局, 可使用 SceneBuilder 程序进行编辑调整
