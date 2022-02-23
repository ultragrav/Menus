package net.ultragrav.menus;

import lombok.Setter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ClickHandlerSet {
    private static final ClickType[] leftClicks = {ClickType.LEFT, ClickType.SHIFT_LEFT};
    private static final ClickType[] rightClicks = {ClickType.RIGHT, ClickType.SHIFT_RIGHT};

    private static final Consumer<InventoryClickEvent> def = e -> e.setCancelled(true);

    @Setter
    private Consumer<InventoryClickEvent> defaultHandler = def;
    private final Map<ClickType, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    public void handle(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> handler = handlers.get(event.getClick());
        if (handler == null) handler = defaultHandler;
        handler.accept(event);
    }

    public ClickHandlerSet leftClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, leftClicks);
    }
    public ClickHandlerSet rightClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, rightClicks);
    }
    public ClickHandlerSet middleClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, ClickType.MIDDLE);
    }
    public ClickHandlerSet defaultHandler(Consumer<InventoryClickEvent> handler) {
        defaultHandler = handler;
        return this;
    }

    public ClickHandlerSet assign(Consumer<InventoryClickEvent> handler, ClickType... types) {
        for (ClickType type : types)
            handlers.put(type, handler);
        return this;
    }
    public ClickHandlerSet assign(Consumer<InventoryClickEvent> handler, Collection<ClickType> types) {
        for (ClickType type : types)
            handlers.put(type, handler);
        return this;
    }
}
