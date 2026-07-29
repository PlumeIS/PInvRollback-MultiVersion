package cn.plumc.invrollback;

import cn.plumc.invrollback.commands.PInvRollbackCommand;
import cn.plumc.invrollback.listeners.GameListener;
import cn.plumc.invrollback.listeners.InventoryListener;
import cn.plumc.invrollback.listeners.RollbackListener;
import cn.plumc.invrollback.nms.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PInvRollback extends JavaPlugin {

    public static PInvRollback instance;
    public static RollbackManager rollbackManager;

    public void saveResourceIfNotExists(String resourceName) {
        File targetFile = new File(getDataFolder(), resourceName);
        if (!targetFile.exists()) {
            saveResource(resourceName, false);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        rollbackManager = new RollbackManager();
        rollbackManager.load(getDataFolder().toPath());
        saveResourceIfNotExists("messages.yml");
        saveDefaultConfig();
        Config.load();

        NMSUtils.createNMSHandler();

        Bukkit.getPluginManager().registerEvents(new GameListener(), this);
        Bukkit.getPluginManager().registerEvents(new RollbackListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        PInvRollbackCommand executor = new PInvRollbackCommand();
        PluginCommand pinvrollback = Bukkit.getPluginCommand("pinvrollback");
        pinvrollback.setExecutor(executor);
        pinvrollback.setTabCompleter(executor);
        getLogger().info("PInvRollback setup complete.");
    }

    @Override
    public void onDisable() {
        rollbackManager.save(getDataFolder().toPath());
    }

}
