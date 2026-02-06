package cn.plumc.invrollback.ui;

import cn.plumc.invrollback.Config;
import cn.plumc.invrollback.PInvRollback;
import cn.plumc.invrollback.RollbackManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RollbackUI extends ChestUI{
    public static HashMap<UUID, RollbackUI> opened = new HashMap<>();

    private static final int PLAYER_HEAD = 4;
    private static final int FILTER = 49;
    private static final int PAGE_PREV = 45;
    private static final int PAGE_NEXT = 53;

    private static final int VIEW_PRE_PAGE = 36;

    private int page = 0;

    private List<RollbackManager.ProfileView> views;
    private List<String> filters;
    private String filterType;
    private int viewStart;

    private final UUID target;

    public RollbackUI(Player player, UUID target) {
        super(player, 54, Config.i18n("ui.rollback.title"));
        this.target = target;
        init();
    }

    @Override
    public void init() {
        views = PInvRollback.rollbackManager.getSortedViews(target);
        filters = PInvRollback.rollbackManager.getTypes(target);
        filters.addFirst(Config.i18n("type.all"));
        filterType = Config.i18n("type.all");
        super.init();
    }

    @Override
    public void update() {
        List<RollbackManager.ProfileView> filtered = views.stream().filter(profileView -> profileView.type().equals(filterType)||filterType.equals(Config.i18n("type.all"))).toList();
        int size = filtered.size();

        int maxPages;
        if (size % VIEW_PRE_PAGE == 0) {
            maxPages = size / VIEW_PRE_PAGE;
        } else {
            maxPages = size / VIEW_PRE_PAGE + 1;
        }
        maxPages-=1;

        viewStart = page * VIEW_PRE_PAGE;
        int viewEnd = Math.min((page + 1) * VIEW_PRE_PAGE, size - 1);

        for (int i = 9; i < 45; i++) inventory.clear(i);

        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");
        for (int i = viewStart; i <= viewEnd; i++) {
            RollbackManager.ProfileView view = filtered.get(i);
            int invIndex = i - viewStart + 9;
            ItemStack viewItem = new ItemStack(Material.CHEST);
            ItemMeta meta = viewItem.getItemMeta();
            meta.setDisplayName(Config.i18n("ui.rollback.view.type").formatted(view.type()));
            List<String> lore = new ArrayList<>(List.of(
                    Config.i18n("ui.rollback.view.id").formatted(view.id()),
                    Config.i18n("ui.rollback.view.date").formatted(format.format(view.date())),
                    Config.i18n("ui.rollback.view.message").formatted("".equals(view.message()) ? Config.i18n("view.message.null") : view.message()),
                    ""
            ));
            if (player.hasPermission("commands.pinvrollback.rollback")){
                lore.add(Config.i18n("ui.rollback.view.tip"));
            } else lore.add(Config.i18n("ui.confirm.view.tip"));
            meta.setLore(lore);
            viewItem.setItemMeta(meta);
            inventory.setItem(invIndex, viewItem);
        }

        ItemStack frame = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setHideTooltip(true);
        frameMeta.setDisplayName("");
        frame.setItemMeta(frameMeta);
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, frame);
        }
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, frame);
        }

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMetal = (SkullMeta) playerHead.getItemMeta();
        headMetal.setOwningPlayer(Bukkit.getOfflinePlayer(target));
        headMetal.setDisplayName(Config.i18n("ui.rollback.player").formatted(player.getName()));
        headMetal.setLore(List.of(
                Config.i18n("ui.rollback.player.count").formatted(views.size()),
                Config.i18n("ui.rollback.player.new_date").formatted(filtered.getFirst() == null ? Config.i18n("view.message.null") : format.format(filtered.getFirst().date()))
        ));
        playerHead.setItemMeta(headMetal);
        inventory.setItem(PLAYER_HEAD, playerHead);

        ItemStack filter = new ItemStack(Material.HOPPER);
        ItemMeta filterMeta = filter.getItemMeta();
        filterMeta.setDisplayName(Config.i18n("ui.rollback.filter"));
        List<String> filterComponents = new ArrayList<>();
        for (String f: filters){
            if (f.equals(filterType)) filterComponents.add("§f" + f);
            else filterComponents.add("§8" + f);
        }
        filterMeta.setLore(filterComponents);
        filter.setItemMeta(filterMeta);
        inventory.setItem(FILTER, filter);

        ItemStack pagePrev = new ItemStack(Material.ARROW);
        ItemMeta pagePrevMeta = pagePrev.getItemMeta();
        if (page==0) pagePrevMeta.setDisplayName(Config.i18n("ui.rollback.previous.disabled").formatted(page + 1, maxPages + 1));
        else pagePrevMeta.setDisplayName(Config.i18n("ui.rollback.previous").formatted(page + 1, maxPages + 1));
        pagePrev.setItemMeta(pagePrevMeta);
        inventory.setItem(PAGE_PREV, pagePrev);

        ItemStack pageNext = new ItemStack(Material.ARROW);
        ItemMeta pageNextMeta = pageNext.getItemMeta();
        if (page==maxPages) pageNextMeta.setDisplayName(Config.i18n("ui.rollback.next.disabled").formatted(page + 1, maxPages + 1));
        else pageNextMeta.setDisplayName(Config.i18n("ui.rollback.next").formatted(page + 1, maxPages + 1));
        pageNext.setItemMeta(pageNextMeta);
        inventory.setItem(PAGE_NEXT, pageNext);
    }

    @Override
    public void onClick(ClickType clickType, InventoryAction action, int slot) {
        List<RollbackManager.ProfileView> filtered = views.stream().filter(profileView -> profileView.type().equals(filterType)||filterType.equals(Config.i18n("type.all"))).toList();
        int size = filtered.size();

        int maxPages;
        if (size % VIEW_PRE_PAGE == 0) {
            maxPages = size / VIEW_PRE_PAGE;
        } else {
            maxPages = size / VIEW_PRE_PAGE + 1;
        }

        if (slot==PAGE_PREV){
            if (page>0) page--;
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1F);
            update();
            return;
        }
        if (slot==PAGE_NEXT){
            if (page<maxPages-1) page++;
            player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1F);
            update();
            return;
        }
        if (slot==FILTER){
            int i = filters.indexOf(filterType);
            i++;
            if (i>filters.size()-1) i = 0;
            filterType = filters.get(i);
            page = 0;
            player.playSound(player, Sound.BLOCK_DISPENSER_FAIL, 0.3F, 1F);
            update();
            return;
        }
        if (9<=slot&&slot<=44){
            int i = slot + viewStart - 9;
            RollbackManager.ProfileView view = filtered.get(i);
            if (clickType == ClickType.LEFT){
                ViewUI viewUI = new ViewUI(this, player, view.id());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 0.3F, 1F);
                Bukkit.getScheduler().runTask(PInvRollback.instance, ()->{onClose();viewUI.open();});
                return;
            }
            if (clickType == ClickType.RIGHT && player.hasPermission("commands.pinvrollback.rollback")) {
                ConfirmUI confirmUI = new ConfirmUI(this, player, view.id());
                player.playSound(player, Sound.BLOCK_CHEST_OPEN, 0.3F, 1F);
                Bukkit.getScheduler().runTask(PInvRollback.instance, ()->{onClose();confirmUI.open();});
                return;
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        opened.remove(player.getUniqueId());
    }

    @Override
    public void open() {
        super.open();
        opened.put(player.getUniqueId(), this);
    }

    public static void open(Player player, UUID target){
        new RollbackUI(player, target).open();
    }

    public static RollbackUI get(Player player, Inventory inventory){
        if (isPlayerOpen(player.getUniqueId(), inventory)) {
            return opened.get(player.getUniqueId());
        }
        return null;
    }
}
