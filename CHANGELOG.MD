# CHANGELOG

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
