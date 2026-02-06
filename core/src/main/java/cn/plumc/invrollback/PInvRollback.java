package cn.plumc.invrollback;

import cn.plumc.invrollback.commands.PInvRollbackCommand;
import cn.plumc.invrollback.listeners.GameListener;
import cn.plumc.invrollback.listeners.InventoryListener;
import cn.plumc.invrollback.listeners.RollbackListener;
import cn.plumc.invrollback.nms.NMSUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PInvRollback extends JavaPlugin {

    public static PInvRollback instance;
    public static RollbackManager rollbackManager;

    @Override
    public void onEnable() {
        instance = this;

        rollbackManager = new RollbackManager();
        rollbackManager.load(getDataFolder().toPath());
        saveResource("messages.yml", false);
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
