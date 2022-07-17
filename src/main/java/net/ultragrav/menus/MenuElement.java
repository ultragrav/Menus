package net.ultragrav.menus;

import lombok.Getter;
import lombok.Setter;
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

    public MenuElement updateInterval(int interval) {
        this.updateInterval = interval;
        return this;
    }

    public MenuElement updater(Consumer<ElementUpdater> updater) {
        this.updateInterval = 2;
        this.updater = updater;
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
}
