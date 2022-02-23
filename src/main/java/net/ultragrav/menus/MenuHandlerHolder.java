package net.ultragrav.menus;

import lombok.Getter;
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
@Setter
public class MenuHandlerHolder {
    private ClickHandlerSet clickHandler;
    private ClickHandlerSet ownClickHandler;

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

        InventoryDragEvent ev = new InventoryDragEvent(
                view,
                event.getCursor(),
                event.getOldCursor(),
                event.getType() == DragType.SINGLE,
                itemsTop
        );
        ev.setCancelled(true);
        InventoryDragEvent ev2 = new InventoryDragEvent(
                view,
                event.getCursor(),
                event.getOldCursor(),
                event.getType() == DragType.SINGLE,
                itemsBottom
        );
        ev2.setCancelled(true);
        if (!itemsTop.isEmpty() && dragHandler != null) dragHandler.accept(ev);
        if (!itemsBottom.isEmpty() && ownDragHandler != null) ownDragHandler.accept(ev2);

        if (!ev.isCancelled() && !ev2.isCancelled()) event.setCancelled(false);
    }
}
