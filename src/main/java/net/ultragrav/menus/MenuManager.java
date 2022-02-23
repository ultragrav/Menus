package net.ultragrav.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

public class MenuManager implements Listener {
    public static MenuManager instance;

    protected Plugin plugin;

    private MenuManager(Plugin plugin) {
        this.plugin = plugin;

        instance = this;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void handleClose(InventoryCloseEvent event) {
        Inventory closedInv = event.getInventory();
        if (closedInv.getHolder() instanceof MenuHolder) {
            Menu menu = ((MenuHolder) closedInv.getHolder()).getMenu();
            menu.onClose(event);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getWhoClicked().getOpenInventory().getTopInventory();
        if (inv.getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
            Menu menu = ((MenuHolder) inv.getHolder()).getMenu();
            menu.handleClick(event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getView().getTopInventory();
        if (inv.getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
            Menu menu = ((MenuHolder) inv.getHolder()).getMenu();
            menu.handleDrag(event);
        }
    }

    /**
     * Reopens the menu for all players that have it open.
     *
     * @param menu The menu to reopen.
     */
    public void reopen(Menu menu) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            if (view == null) continue;
            Inventory top = view.getTopInventory();
            if (top == null) continue;
            if (top.getHolder() instanceof MenuHolder) {
                if (((MenuHolder) top.getHolder()).getMenu() == menu) {
                    menu.open(player);
                }
            }
        }
    }
}
