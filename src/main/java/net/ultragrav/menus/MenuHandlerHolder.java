package net.ultragrav.menus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class MenuHandlerHolder {
    private final Menu parent;

    private ClickHandlerSet<MenuHandlerHolder> clickHandler;
    private ClickHandlerSet<MenuHandlerHolder> ownClickHandler;

    private Consumer<InventoryDragEvent> dragHandler;
    private Consumer<InventoryDragEvent> ownDragHandler;

    public void handleClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        InventoryView view = event.getWhoClicked().getOpenInventory();
        if (clicked == null || clicked.equals(view.getTopInventory())) {
            if (clickHandler != null)
                clickHandler.handle(event);
        } else if (clicked.equals(view.getBottomInventory())) {
            if (ownClickHandler != null)
                ownClickHandler.handle(event);
        }
    }

    /**
     * Called when an item is dragged in an inventory.
     *
     * @param event The event.
     */
    public void handleDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        Inventory bottom = view.getBottomInventory();

        int topSize = top.getSize();

        Map<Integer, ItemStack> itemsTop = new HashMap<>();
        Map<Integer, ItemStack> itemsBottom = new HashMap<>();

        for (Map.Entry<Integer, ItemStack> ent : event.getNewItems().entrySet()) {
            if (ent.getKey() < topSize) {
                itemsTop.put(ent.getKey(), ent.getValue());
            } else {
                itemsBottom.put(ent.getKey() - topSize, ent.getValue());
            }
        }

        boolean cancelled = dragHandler == null && ownDragHandler == null;
        if (!itemsTop.isEmpty() && dragHandler != null) {
            InventoryDragEvent ev = new InventoryDragEvent(
                    view,
                    event.getCursor(),
                    event.getOldCursor(),
                    event.getType() == DragType.SINGLE,
                    itemsTop
            );
            ev.setCancelled(true);

            dragHandler.accept(ev);

            if (ev.isCancelled())
                cancelled = true;
        }

        if (!itemsBottom.isEmpty() && ownDragHandler != null) {
            InventoryDragEvent ev2 = new InventoryDragEvent(
                    view,
                    event.getCursor(),
                    event.getOldCursor(),
                    event.getType() == DragType.SINGLE,
                    itemsBottom
            );
            ev2.setCancelled(true);

            ownDragHandler.accept(ev2);

            if (ev2.isCancelled())
                cancelled = true;
        }

        event.setCancelled(cancelled);
    }

    public ClickHandlerSet<MenuHandlerHolder> defaultClickHandler() {
        return (clickHandler = new ClickHandlerSet<>(this));
    }
    public ClickHandlerSet<MenuHandlerHolder> ownInventoryClickHandler() {
        return (ownClickHandler = new ClickHandlerSet<>(this));
    }

    public MenuHandlerHolder defaultDragHandler(Consumer<InventoryDragEvent> handler) {
        dragHandler = handler;
        return this;
    }
    public MenuHandlerHolder ownInventoryDragHandler(Consumer<InventoryDragEvent> handler) {
        ownDragHandler = handler;
        return this;
    }

    public Menu finish() {
        return parent;
    }
}
