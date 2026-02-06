package cn.plumc.invrollback;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    public static YamlConfiguration messages;

    public static void load(){
        messages = YamlConfiguration.loadConfiguration(new File(PInvRollback.instance.getDataFolder(), "messages.yml"));
    }

    public static String i18n(String key){
        String text = messages.getString(key.replaceAll("\\.", "_"), key).replaceAll("&", "§");
        if (key.startsWith("command")) return getPrefix()+" "+text;
        return text;
    }

    public static int maxCount(String type){
        return PInvRollback.instance.getConfig().getInt("maxSaves.%s".formatted(type));
    }

    public static int pageLines(){
        return PInvRollback.instance.getConfig().getInt("command.page_lines");
    }

    public static String getPrefix(){
        return messages.getString("command_prefix", "").replaceAll("&", "§");
    }
}
