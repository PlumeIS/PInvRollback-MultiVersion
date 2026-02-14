package cn.plumc.invrollback.nms;

import cn.plumc.invrollback.PInvRollback;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMSUtils {
    private static final String LAST_VERSION = "1.21.3";

    private static NMSHandler nmsHandler;

    private static String minecraftVersion;

    private static boolean isRunOnPaper() {
        try {
            Class.forName("com.destroystokyo.paper.Namespaced");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns the actual running Minecraft version, e.g. 1.20 or 1.16.5
     *
     * @return Minecraft version
     */
    private static String getMinecraftVersion() {
        if (minecraftVersion != null) {
            return minecraftVersion;
        } else {
            String bukkitGetVersionOutput = Bukkit.getVersion();
            Matcher matcher = Pattern.compile("\\(MC: (?<version>[\\d]+\\.[\\d]+(\\.[\\d]+)?)\\)").matcher(bukkitGetVersionOutput);
            if (matcher.find()) {
                return minecraftVersion = matcher.group("version");
            } else {
                throw new RuntimeException("Could not determine Minecraft version from Bukkit.getVersion(): " + bukkitGetVersionOutput);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends NMSHandler> getNMSHandlerClazz() throws RuntimeException, ClassNotFoundException {
        String clazzName;
        if (isRunOnPaper()) {
            clazzName = "cn.plumc.invrollback.nms.paper.NMSHandlerImpl";
        } else {
            clazzName = "cn.plumc.invrollback.nms.v" + getMinecraftVersion()
                    .replace(".", "_") + ".NMSHandlerImpl";
        }

        try {
            return (Class<? extends NMSHandler>) Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            PInvRollback.instance.getLogger().warning("Could not match current version:" + getMinecraftVersion());
            PInvRollback.instance.getLogger().warning("Try using the latest version:" + LAST_VERSION);
            return (Class<? extends NMSHandler>) Class.forName("cn.plumc.invrollback.nms.v" + LAST_VERSION
                    .replace(".", "_") + ".NMSHandlerImpl");
        }

    }

    @SuppressWarnings("uncheck")
    public static void createNMSHandler() {
        try {
            nmsHandler = getNMSHandlerClazz().getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            PInvRollback.instance.getLogger().warning("Could not create NMS handler: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(PInvRollback.instance);
        }
    }


    public static NMSHandler getNMSHandler() {
        return nmsHandler;
    }
}
