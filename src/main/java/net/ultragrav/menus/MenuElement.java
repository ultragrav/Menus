package net.ultragrav.menus;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@Getter
public class MenuElement {
    private final ClickHandlerSet handler;
    private final ItemStack item;

    private final ElementUpdater updaterInstance;
    private Consumer<ElementUpdater> updater;
    private int updateInterval = -1;

    public MenuElement(ItemStack item) {
        this.item = item;

        this.handler = new ClickHandlerSet();
        this.updaterInstance = new ElementUpdater(this);
    }

    /**
     * Sets the handler for left clicks.
     *
     * @param handler The handler.
     * @return This MenuElement.
     */
    public MenuElement leftClick(Consumer<InventoryClickEvent> handler) {
        this.handler.leftClick(handler);
        return this;
    }

    /**
     * Sets the handler for all clicks.
     *
     * @param handler The handler.
     * @return This MenuElement.
     */
    public MenuElement setClickHandler(Consumer<InventoryClickEvent> handler) {
        this.handler.defaultHandler(handler);
        return this;
    }

    public MenuElement updateInterval(int interval) {
        this.updateInterval = interval;
        return this;
    }

    public MenuElement updater(Consumer<ElementUpdater> updater) {
        this.updater = updater;
        return this;
    }

    private int updateTicker = 0;

    public void update() {
        if (updateInterval == -1) return;

        if (updateTicker++ >= updateInterval) {
            updateTicker = 0;
            updater.accept(this.updaterInstance);
        }
    }
}
