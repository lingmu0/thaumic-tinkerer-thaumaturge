# 神秘工匠：Thaumaturge 非官方移植

以原版 **Thaumic Tinkerer 1.7.10（含 KAMI）** 为内容基准，分别适配 Minecraft 1.21.1 / 26.1.2 NeoForge。模组 ID：`thaumictinkerer`。当前为 `0.1.1-dev` 开发版。

`0.1.1-dev` 修复烟熏石英缺失纹理、机器模型 UV、26.1.2 方块物品及六种元素火瓶的名称。更新时移走旧版 jar，只保留当前游戏版本的一份新 jar，然后重启游戏。详见 [更新记录](docs/CHANGELOG.md)。

原版使用 **CC BY-NC-SA 3.0**，允许移植，须署名、非商业使用、以相同许可共享并注明修改。原作者、源码版本与完整许可见 [LICENSE](LICENSE)、[许可核查](docs/LICENSE-AUDIT.md)。本项目未采用 Reborn 的代码或新资产。

| 游戏 | Java | 编译 NeoForge | 必须安装的 Thaumaturge | Curios |
| --- | --- | --- | --- | --- |
| 1.21.1 | 21 | 21.1.238 | 官方 `1.21.1` 源码分支本地构建的 1.0.4 | 9.5.1 或更新的同游戏版本 |
| 26.1.2 | 25 | 26.1.2.112 | 官方公开发布的 1.0.2 | 15.0.0 或更新的同游戏版本 |

还须安装 Thaumaturge 自己声明的前置，例如 Lithostitched。两个游戏版本的 jar 不能混用。当前锁定已核对的 Thaumaturge 版本，防止其尚在变化的 API 导致加载错误。

**1.21.1 前置尚无公开发布包。** 本工作区已按官方 CI 的 `runData` → `jar` 顺序构建该分支；仅供本机开发使用，位置为项目旁 `.thaumaturge-1.21.1-upstream/build/libs/`。它不包含在本项目或交付压缩包内，也不能作为本附属的组件再分发。更换前置版本后需要重新编译、核查 API。

已实现机器、十种核心、十四种附魔、灵魂系统、元素火焰/作物/药水、KAMI 材料/装备/工具/法杖部件、传送网关及基岩维度。旧版槽位改用 Curios，核心接入 Thaumaturge 的模块化法术系统，部分旧 GUI 改为手持/右键交互。实现范围及差异见 [移植说明](docs/PORTING.md) 和 `catalog/`。

这是开发构建，已进行编译、打包和静态资源检查，**尚未进行客户端、专用服务器或存档实测**；不能据此宣称完整复刻或生产环境稳定。尚未复刻的视觉和可选模组联动见移植说明。

## 构建

已生成的 Java/资源均随源码提供，普通构建不需要重新运行生成脚本。

```powershell
# 1.21.1：JAVA_HOME 指向 JDK 21
cd neoforge-1.21.1
.\gradlew.bat build '-Pthaumaturge_jar=前置jar的绝对路径'

# 26.1.2：JAVA_HOME 指向 JDK 25
cd ..\neoforge-26.1.2
.\gradlew.bat build
```

26.1.2 默认使用官方 CurseForge 文件 9095192；也可用相同的 `-Pthaumaturge_jar=...` 参数指定该发布版 jar。前置仅在编译时引用，不会打进附属 jar。产物在各版本 `build/libs/`。

资源再生成需要将原版 [1.7.10 源码](https://github.com/Thaumic-Tinkerer/ThaumicTinkerer/tree/1.7.10) 放在项目旁 `.thaumic-tinkerer-1.7.10-upstream/`，并先建立开发依赖资源索引：

```powershell
.\tools\index-dependencies.ps1 -Version 1.21.1 -JarPath '前置jar路径'
.\tools\index-dependencies.ps1 -Version 26.1.2 -JarPath '前置jar路径'
node tools/generate-all.cjs
node tools/validate-resources.cjs
```

`tools/build-upstream.cjs` 只辅助本机生成、构建官方 1.21.1 分支，不能发布前置。检查记录在 `validation/`；经验护符另有不启动游戏的 XP 守恒检查。
