# PInvRollback

[English Document](README.md)

PInvRollback 是一个 Minecraft 插件，旨在帮助服务器管理员安全地管理和回滚玩家的背包和末影箱。它支持多种服务器版本（Spigot/Paper）。

## 功能

- **背包和末影箱回滚**: 高效地保存和恢复玩家的物品。
- **手动备份**: 玩家或管理员可以手动创建备份。
- **图形用户界面 (GUI)**: 通过游戏内菜单轻松管理备份。
- **多版本支持**: 兼容 Spigot 1.21.1, 1.21.3 和 Paper 环境。

## 模块

该项目分为以下模块，以确保跨版本兼容性：

*   **core**: 包含核心逻辑和抽象实现。
*   **dist**: 处理最终插件 jar 的打包和分发。
*   **spigot-1.21.1**: 针对 Spigot 1.21.1 的版本特定实现。
*   **spigot-1.21.3**: 针对 Spigot 1.21.3 的版本特定实现。
*   **paper**: Paper 特定的实现和优化。

## 构建插件

该项目使用 Maven 进行依赖管理和构建。

要在根目录中构建项目，请运行以下命令：

```bash
mvn clean install
```

编译后的插件 jar 通常位于 `dist/target` 目录中。

## 命令

插件的主要命令是 `/pinvrollback`（或配置的别名）。

*   `/pinvrollback create [message] [player]` - 创建背包的手动备份。
*   `/pinvrollback rollback <id> [reason] [player]` - 回滚到特定的备份 ID。
*   `/pinvrollback list [page] [player]` - 列出玩家所有可用的备份。
*   `/pinvrollback ui [player]` - 打开用于管理备份的图形用户界面。

## 权限

以下是与插件命令相关的权限：

*   `commands.pinvrollback` - 使用任何插件命令的基本权限。
*   `commands.pinvrollback.create` - 创建手动备份的权限。
*   `commands.pinvrollback.rollback` - 执行回滚的权限。
*   `commands.pinvrollback.list` - 列出备份的权限。
*   `commands.pinvrollback.ui` - 打开备份管理 GUI 的权限。

**管理员权限:**
*   `commands.create.other` - 为其他玩家创建备份。
*   `commands.pinvrollback.rollback.other` - 为其他玩家执行回滚。
*   `commands.list.other` - 查看其他玩家的备份。
*   `commands.pinvrollback.ui.other` - 为其他玩家打开 GUI。
