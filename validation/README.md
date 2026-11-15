# 验证记录

最终构建日期：2026-11-15。两个版本均执行 `build` 成功，包含编译、资源处理、普通 jar 与 sources jar 打包。

| 检查 | 结果及范围 |
| --- | --- |
| 1.21.1 构建 | JDK 21、NeoForge 21.1.238、官方 Thaumaturge 1.1.0 文件 9452543 |
| 26.1.2 构建 | JDK 25、NeoForge 26.1.2.112、官方 Thaumaturge 1.1.0 文件 9428045 |
| 静态资源 | 检查物品模型/纹理、配方、要素、研究引用和依赖环；具体成品数量见 package-report.json |
| 客户端资产回归 | 覆盖全部144个注册物品、模型继承/纹理变量、合法小写资源ID、blockstate模型、现代item定义及中英文名称；明细见client-assets两份报告 |
| 模型离线预览 | 检查修复台、两种磁体及其他设备的原生JSON纹理取样渲染，预览保存在docs/device-model-*.png；不等于游戏内复测 |
| 成品审计 | 许可、展开后的版本元数据、目标 Java 字节码、26 版物品定义齐全；没有捆绑 Thaumaturge/Thaumcraft/AE 代码或 Thaumaturge 资产 |
| XP 守恒 | 2026-10-18的独立Java检查通过90,060组组合；本次资源修复未修改该逻辑 |
| Mixin 静态检查 | 两版本 ExperienceOrb 均存在私有 int count，访问器路径及 mixin 元数据已包含于成品；未进行运行时注入测试 |
| 模板保护 | 原 demo / demo1.21.1 仅作读取来源，无本次任务修改 |

`resource-report.json` 和 `package-report.json` 是工具生成的机器记录；最终 jar SHA-256 以成品报告为准。

Gradle 显示 `test NO-SOURCE`：没有把编译成功描述为游戏测试或完整单元测试套件通过。XP 守恒检查是独立执行的检查程序。

1.21.1 前置的旧源质接口有弃用/计划移除警告，当前固定前置版本仍提供对应桥接。后续前置升级须复查；这不是构建错误。

没有执行 runClient / runServer 或打开游戏世界。未验证客户端渲染、服务器启动、数据包实际解码、研究交互、多人容器操作、机器供给、施法、维度生成及其他模组兼容性。

重跑 XP 检查（JDK 21 或更新）：

```powershell
New-Item -ItemType Directory -Force validation/test-classes
javac -d validation/test-classes common/src/main/java/net/lingmu/thaumictinkerer/ExperienceTransfer.java validation/ExperienceTransferCheck.java
java -cp validation/test-classes ExperienceTransferCheck
```

资源校验需要先按主 README 为两份实际前置 jar 建立本机索引。成品审计运行 `./tools/audit-packages.ps1`。
