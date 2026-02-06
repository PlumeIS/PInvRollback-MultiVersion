package cn.plumc.invrollback.commands;

import cn.plumc.invrollback.Config;
import cn.plumc.invrollback.PInvRollback;
import cn.plumc.invrollback.RollbackManager;
import cn.plumc.invrollback.profile.EnderChestProfile;
import cn.plumc.invrollback.profile.InventoryProfile;
import cn.plumc.invrollback.ui.RollbackUI;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PInvRollbackCommand implements TabExecutor {
    private enum SubCommands{
        ROLLBACK,
        CREATE,
        LIST,
        UI
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Config.i18n("command.player_only"));
            return false;
        }

        if (!sender.hasPermission("commands.pinvrollback")){
            sender.sendMessage(Config.i18n("command.permission_missing"));
            return true;
        }

        if (args.length == 0) return helper(sender, null);

        if (args[0].equalsIgnoreCase("rollback")) {
            if (sender.hasPermission("commands.pinvrollback.rollback")) return rollback(player, args);
            else {
                sender.sendMessage(Config.i18n("command.permission_missing"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("create")){
            if (sender.hasPermission("commands.pinvrollback.create")) return create(player, args);
            else {
                sender.sendMessage(Config.i18n("command.permission_missing"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("list")){
            if (sender.hasPermission("commands.pinvrollback.list")) return list(player, args);
            else {
                sender.sendMessage(Config.i18n("command.permission_missing"));
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("ui") && sender.hasPermission("commands.pinvrollback.ui")){
            return ui(player, args);
        }

        return false;
    }

    private boolean rollback(Player player, String[] args){
        if (args.length == 1) return helper(player, SubCommands.ROLLBACK);
        if (args.length >= 2) {
            if (NumberUtils.isParsable(args[1])){
                String reason = "";
                if (args.length >= 3) reason = args[2];
                if (args.length == 2 || args.length == 3) {
                    if (!PInvRollback.rollbackManager.getOwner(NumberUtils.createInteger(args[1])).equals(player.getUniqueId())&&
                            !(args.length == 3 && args[2].equals("force"))){
                        player.sendMessage(Config.i18n("command.rollback.failed"));
                        return true;
                    }
                    PInvRollback.rollbackManager.rollback(player, NumberUtils.createInteger(args[1]), reason);
                    player.sendMessage(Config.i18n("command.rollback.success"));
                    return true;
                } else if (args.length==4) {
                    if (player.hasPermission("commands.pinvrollback.rollback.other")) {
                        OfflinePlayer target = Bukkit.getOfflinePlayer(args[3]);
                        if (!target.isOnline()) {
                            player.sendMessage(Config.i18n("command.player.offline"));
                        } else {
                            if (!PInvRollback.rollbackManager.getOwner(NumberUtils.createInteger(args[1])).equals(target.getUniqueId())&&!args[2].equals("force")) {
                                player.sendMessage(Config.i18n("command.rollback.failed.other"));
                                return true;
                            }
                            PInvRollback.rollbackManager.rollback(target.getPlayer(), NumberUtils.createInteger(args[1]), reason);
                            player.sendMessage(Config.i18n("command.rollback.success"));
                        }
                        return true;
                    } else {
                        player.sendMessage(Config.i18n("command.permission_missing"));
                        return true;
                    }
                } else {
                    return helper(player, SubCommands.ROLLBACK);
                }
            } else {
                player.sendMessage(Config.i18n("command.rollback.id.missing"));
                return true;
            }
        }
        return helper(player, SubCommands.ROLLBACK);
    }

    private boolean create(Player player, String[] args){
        String message = "";
        if (args.length >= 2) message = args[1];
        if (args.length == 1 || args.length == 2) {
            InventoryProfile inventoryProfile = InventoryProfile.parse(player.getInventory());
            EnderChestProfile enderChestProfile = EnderChestProfile.parse(player.getEnderChest());
            long id = PInvRollback.rollbackManager.create(player, RollbackManager.DefaultType.MANUAL, message, Config.maxCount("manual"), inventoryProfile, enderChestProfile);
            player.sendMessage(Config.i18n("command.create.success").formatted(id));
        } else if (args.length == 3) {
            if (player.hasPermission("commands.create.other")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                if (!target.isOnline()) {
                    player.sendMessage(Config.i18n("command.player.offline"));
                } else {
                    InventoryProfile inventoryProfile = InventoryProfile.parse(target.getPlayer().getInventory());
                    EnderChestProfile enderChestProfile = EnderChestProfile.parse(target.getPlayer().getEnderChest());
                    long id = PInvRollback.rollbackManager.create(target.getPlayer(), RollbackManager.DefaultType.MANUAL, message, Config.maxCount("manual"), inventoryProfile, enderChestProfile);
                    player.sendMessage(Config.i18n("command.create.success").formatted(id));
                }
            } else {
                player.sendMessage(Config.i18n("command.permission_missing"));
                return true;
            }
        }
        return true;
    }

    private boolean list(Player player, String[] args){
        int page = 0;
        if (args.length >= 2) page = NumberUtils.createInteger(args[1])-1;

        String name = "";
        int pages = 0;
        List<RollbackManager.ProfileView> sortedViews = null;

        if (args.length == 1 || args.length == 2){
            name = player.getName();
            sortedViews = PInvRollback.rollbackManager.getSortedViews(player.getUniqueId());
        } else if (args.length == 3){
            if (player.hasPermission("commands.list.other")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
                name = target.getName();
                sortedViews = PInvRollback.rollbackManager.getSortedViews(target.getUniqueId());
            } else {
                player.sendMessage(Config.i18n("command.permission_missing"));
                return true;
            }
        }

        int start = page * Config.pageLines();
        int end = start + Config.pageLines();
        if (sortedViews.size()-1 < end){
            end = sortedViews.size()-1;
        }

        if (sortedViews.size()%Config.pageLines() == 0){
            pages = sortedViews.size()/Config.pageLines()-1;
        } else {
            pages = sortedViews.size()/Config.pageLines();
        }

        if (page < 0 || page > pages){
            player.sendMessage(Config.i18n("command.list.out_of_pages"));
            return true;
        }

        SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm:ss");
        player.sendMessage(Config.i18n("command.list.prefix").formatted(name, page+1, pages+1));
        for (int i = start; i <= end; i++){
            RollbackManager.ProfileView view = sortedViews.get(i);
            String message = view.message().isEmpty() ? Config.i18n("view.message.null") : view.message();
            if (message.length()>13) message = message.substring(0, 10)+"...";
            player.sendMessage(Config.i18n("command.list.line").formatted(view.id(), view.type(), format.format(view.date()), message));
        }
        return true;
    }

    private boolean ui(Player player, String[] args){
        if (args.length == 1){
            RollbackUI.open(player, player.getUniqueId());
            return true;
        } else if (args.length >= 2 || player.hasPermission("commands.pinvrollback.ui.other")){OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (!target.isOnline()) {
                player.sendMessage(Config.i18n("command.player.offline"));
            } else {
                RollbackUI.open(player, target.getUniqueId());
            }
        } else {
            player.sendMessage(Config.i18n("command.permission_missing"));
            return true;
        }
        return true;
    }

    private boolean helper(CommandSender sender, SubCommands subCommand){
        if (subCommand==null){
            sender.sendMessage(Config.i18n("command.help"));
            return true;
        }
        switch (subCommand){
            case ROLLBACK:
                sender.sendMessage(Config.i18n("command.help.rollback"));
            case CREATE:
                sender.sendMessage(Config.i18n("command.help.create"));
            case LIST:
                sender.sendMessage(Config.i18n("command.help.list"));
            case UI:
                sender.sendMessage(Config.i18n("command.help.ui"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();
        List<String> subCommands = new ArrayList<>();
        if (sender.hasPermission("commands.pinvrollback.create")) subCommands.add("create");
        if (sender.hasPermission("commands.pinvrollback.rollback")) subCommands.add("rollback");
        if (sender.hasPermission("commands.pinvrollback.list")) subCommands.add("list");
        if (sender.hasPermission("commands.pinvrollback.ui")) subCommands.add("ui");
        if (subCommands.isEmpty()) return new ArrayList<>();
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        List<String> ids = PInvRollback.rollbackManager.getActiveId().stream().map(String::valueOf).toList();
        if (args.length == 1) return subCommands;
        switch (args[0]){
            case "create":{
                if (args.length==2) return new ArrayList<>();
                if (args.length==3 && sender.hasPermission("commands.pinvrollback.create.other")) return players;
            }
            case "rollback":{
                if (args.length==2) return ids;
                if (args.length==3) return new ArrayList<>();
                if (args.length==4 && sender.hasPermission("commands.pinvrollback.rollback.other")) return players;
            }
            case "list":{
                if (args.length==2) {
                    List<RollbackManager.ProfileView> sortedViews = PInvRollback.rollbackManager.getSortedViews(player.getUniqueId());
                    int pages;
                    if (sortedViews.size()%Config.pageLines() == 0){
                        pages = sortedViews.size()/Config.pageLines()-1;
                    } else {
                        pages = sortedViews.size()/Config.pageLines();
                    }
                    List<String> page = new ArrayList<>();
                    for (int i = 1; i <= pages+1; i++) {
                        page.add(String.valueOf(i));
                    }
                    return page;
                }
                if (args.length==3 && sender.hasPermission("commands.pinvrollback.list.other")) return players;
            }
        }
        return new ArrayList<>();
    }

}
