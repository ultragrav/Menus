package net.ultragrav.menus;

import lombok.Getter;
import lombok.Setter;
import net.ultragrav.menus.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class MenuElement {
    private final ClickHandlerSet<MenuElement> handler;
    @Setter
    private ItemStack item;

    private final ElementUpdater updaterInstance;
    private Consumer<ElementUpdater> updater;
    private int updateInterval = -1;

    public MenuElement(ItemStack item) {
        this.item = item;

        this.handler = new ClickHandlerSet<>(this);
        this.updaterInstance = new ElementUpdater(this);
    }

    public ClickHandlerSet<MenuElement> clickBuilder() {
        return handler;
    }

    public MenuElement setClickHandler(Consumer<InventoryClickEvent> handler) {
        this.handler.defaultHandler(handler);
        return this;
    }

    public MenuElement setUpdateEvery(int interval) {
        this.updateInterval = interval;
        return this;
    }
    public MenuElement updateInterval(int interval) {
        this.updateInterval = interval;
        return this;
    }

    public MenuElement updater(Consumer<ElementUpdater> updater) {
        this.updateInterval = 2;
        this.updater = updater;
        return this;
    }
    public MenuElement setUpdateHandler(Consumer<MenuElement> updater) {
        this.updateInterval = 2;
        this.updater = ud -> ud.apply(updater);
        return this;
    }

    private int updateTicker = 0;

    public boolean update() {
        if (updateInterval == -1) return false;

        if (updateTicker++ >= updateInterval) {
            updateTicker = 0;
            updater.accept(this.updaterInstance);
            return true;
        }
        return false;
    }

    /**
     * Adds a temporary lore
     *
     * @param menu  The menu this element is assigned to
     * @param lore  The lore to add
     * @param ticks The length of time in ticks (20ths of a second) that it will stay in effect
     */
    public void addTempLore(Menu menu, String lore, int ticks) {
        int index = menu.indexOfElement(this);
        if (index == -1 || this.getItem() == null) {
            return;
        }
        this.setItem(new ItemBuilder(this.getItem()).addLore(lore).build());
        MenuManager.instance.invalidateElementsInInvForMenu(menu, index);
        Bukkit.getScheduler().scheduleSyncDelayedTask(MenuManager.instance.plugin, () -> {
            MenuElement.this.setItem(new ItemBuilder(MenuElement.this.getItem()).removeLore(lore, false).build());
            MenuManager.instance.invalidateElementsInInvForMenu(menu, index);
        }, ticks);
    }
}
