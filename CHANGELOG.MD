# CHANGELOG

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
