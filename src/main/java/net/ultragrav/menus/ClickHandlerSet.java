package net.ultragrav.menus;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ClickHandlerSet<T> {
    private static final ClickType[] leftClicks = {ClickType.LEFT, ClickType.SHIFT_LEFT};
    private static final ClickType[] rightClicks = {ClickType.RIGHT, ClickType.SHIFT_RIGHT};

    private static final Consumer<InventoryClickEvent> def = e -> e.setCancelled(true);

    @Setter
    private Consumer<InventoryClickEvent> defaultHandler = def;
    private final Map<ClickType, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    private final Object parent;

    public void handle(InventoryClickEvent event) {
        Consumer<InventoryClickEvent> handler = handlers.get(event.getClick());
        if (handler == null) handler = defaultHandler;
        handler.accept(event);
    }

    public ClickHandlerSet<T> leftClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, leftClicks);
    }
    public ClickHandlerSet<T> rightClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, rightClicks);
    }
    public ClickHandlerSet<T> middleClick(Consumer<InventoryClickEvent> handler) {
        return assign(handler, ClickType.MIDDLE);
    }
    public ClickHandlerSet<T> drop(Consumer<InventoryClickEvent> handler) {
        return assign(handler, ClickType.DROP);
    }
    public ClickHandlerSet<T> defaultHandler(Consumer<InventoryClickEvent> handler) {
        defaultHandler = handler;
        return this;
    }

    public ClickHandlerSet<T> assign(Consumer<InventoryClickEvent> handler, ClickType... types) {
        for (ClickType type : types)
            handlers.put(type, handler);
        return this;
    }
    public ClickHandlerSet<T> assign(Consumer<InventoryClickEvent> handler, Collection<ClickType> types) {
        for (ClickType type : types)
            handlers.put(type, handler);
        return this;
    }

    public T build() {
        return (T) parent;
    }
}
