# PInvRollback

[中文文档](README_zh.md)

PInvRollback is a Minecraft plugin designed to help server administrators manage and rollback player inventories and ender chests safely. It supports multiple server versions (Spigot/Paper).

## Features

- **Inventory and Ender Chest Rollback**: Save and restore players' items efficiently.
- **Manual Backups**: Players or admins can manually create backups.
- **Graphical User Interface (GUI)**: Manage backups easily through an in-game menu.
- **Multi-Version Support**: Compatible with Spigot 1.21.1, 1.21.3, and Paper environments.

## Modules

This project is structured into the following modules to ensure cross-version compatibility:

*   **core**: Contains the core logic and abstract implementations.
*   **dist**: Handles the packaging and distribution of the final plugin jar.
*   **spigot-1.21.1**: Version-specific implementations for Spigot 1.21.1.
*   **spigot-1.21.3**: Version-specific implementations for Spigot 1.21.3.
*   **paper**: Paper-specific implementations and optimizations.

## Building the Plugin

This project uses Maven for dependency management and building.

To build the project, run the following command in the root directory:

```bash
mvn clean install
```

The compiled plugin jar will typically be located in the `dist/target` directory.

## Commands

The main command for the plugin is `/pinvrollback` (or aliases if configured).

*   `/pinvrollback create [message] [player]` - Create a manual backup of the inventory.
*   `/pinvrollback rollback <id> [reason] [player]` - Rollback to a specific backup ID.
*   `/pinvrollback list [page] [player]` - List all available backups for a player.
*   `/pinvrollback ui [player]` - Open the graphical user interface for managing backups.

## Permissions

Here are the permissions associated with the plugin commands:

*   `commands.pinvrollback` - Base permission for using any plugin command.
*   `commands.pinvrollback.create` - Permission to create manual backups.
*   `commands.pinvrollback.rollback` - Permission to perform rollbacks.
*   `commands.pinvrollback.list` - Permission to list backups.
*   `commands.pinvrollback.ui` - Permission to open the backup management GUI.

**Admin Permissions:**
*   `commands.create.other` - Create backups for other players.
*   `commands.pinvrollback.rollback.other` - Perform rollbacks for other players.
*   `commands.list.other` - View backups of other players.
*   `commands.pinvrollback.ui.other` - Open the GUI for other players.
