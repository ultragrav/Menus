package net.ultragrav.menus;

import net.ultragrav.menus.shapes.MenuRect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.*;

public abstract class Menu {
    private final MenuHandlerHolder handler = new MenuHandlerHolder();

    private String title;
    private int rows;
    private final Map<Integer, MenuElement> elements = new HashMap<>();

    private int taskId = -1;
    private final List<UUID> viewers = new ArrayList<>();

    public Menu(String title, int rows) {
        this.title = title;
        this.rows = rows;
    }

    public Menu() {
        this("", 3);
    }

    public abstract void setup(HumanEntity player);

    public void onClose(InventoryCloseEvent event) {
        viewers.remove(event.getPlayer().getUniqueId());
        if (viewers.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Creates an inventory with the given title and rows.
     *
     * @return The inventory.
     */
    public Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(new MenuHolder(this), rows * 9, ChatColor.translateAlternateColorCodes('&', title));
        for (int i = 0; i < rows * 9; i++) {
            MenuElement e = elements.get(i);
            if (e == null) {
                continue;
            }
            inv.setItem(i, e.getItem());
        }
        return inv;
    }

    /**
     * Opens the inventory for the given player.
     *
     * @param player The player to open the inventory for.
     */
    public void open(HumanEntity player) {
        if (player == null) return;

        if (player.getOpenInventory() != null)
            MenuManager.instance.handleClose(new InventoryCloseEvent(player.getOpenInventory()));

        setup(player);

        Inventory inv = createInventory();
        viewers.add(player.getUniqueId());
        if (taskId == -1) {
            // We only do updates 10 times per second since more is unnecessary
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MenuManager.instance.plugin, this::update, 2, 2);
        }
        Bukkit.getScheduler().runTask(MenuManager.instance.plugin, () -> player.openInventory(inv));
    }

    private void update() {
        for (MenuElement element : this.elements.values())
            element.update();
    }

    /**
     * Handles the click event.
     *
     * @param event The event.
     */
    public void handleClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            handler.handleClick(event);
            return;
        }

        int slot = event.getSlot();
        if (clicked.equals(event.getWhoClicked().getOpenInventory().getTopInventory()) && elements.containsKey(slot)) {
            elements.get(slot).getHandler().handle(event);
        } else {
            handler.handleClick(event);
        }
    }

    /**
     * Handles the drag event.
     *
     * @param event The event.
     */
    public void handleDrag(InventoryDragEvent event) {
        handler.handleDrag(event);
    }

    /**
     * Sets the element at the given slot.
     *
     * @param slot    The slot.
     * @param element The element.
     * @return The element.
     */
    public MenuElement setElement(int slot, MenuElement element) {
        elements.put(slot, element);
        return element;
    }

    public MenuElement getElement(int slot) {
        return elements.get(slot);
    }

    /**
     * Sets the elements in the given row.
     *
     * @param row     The row.
     * @param element The element.
     */
    public void setRow(int row, MenuElement element) {
        for (int i = row * 9; i < (row + 1) * 9; i++) {
            setElement(i, element);
        }
    }

    public void clear() {
        this.elements.clear();
    }

    public void setAll(MenuElement element) {
        for (int i = 0; i < rows * 9; i++)
            setElement(i, element);
    }

    public void setEmpty(MenuElement element) {

    }

    public MenuRect rect(int minRow, int minColumn, int maxRow, int maxColumn) {
        return new MenuRect(this, minRow, minColumn, maxRow, maxColumn);
    }

    /**
     * Fills the given rectangle with the given elements.
     *
     * @param startRow    The start row.
     * @param startColumn The start column.
     * @param endRow      The end row.
     * @param endColumn   The end column.
     * @param elementList The element list.
     */
    public void fillRect(int startRow, int startColumn, int endRow, int endColumn, List<MenuElement> elementList) {
        //Reordering
        int min = Math.min(startColumn, endColumn);
        int max = Math.max(startColumn, endColumn);
        startColumn = min;
        endColumn = max;

        min = Math.min(startRow, endRow);
        max = Math.max(startRow, endRow);
        startRow = min;
        endRow = max;

        //Place elements
        int element = 0;
        int endIndex = endRow * 9 + endColumn;
        for (int index = startRow * 9 + startColumn; index <= endIndex; index++) {
            int col = index % 9;
            if (col > endColumn) { //End of column
                index += 9 - (endColumn - startColumn + 1); //Skip to next row
            }

            if (element >= elementList.size())
                break;

            setElement(index, elementList.get(element++));
        }
    }

    /**
     * Distributes the given elements evenly in the given row.
     *
     * @param row      The row.
     * @param elements The elements.
     */
    public void evenlyDistribute(int row, MenuElement... elements) {
        int size = elements.length;
        if (size == 0)
            return;

        if (size <= Math.ceil((9 * 2) / 2d)) {
            int fromMiddle = size - 1;
            int i1 = 0;
            for (int i = 4 - fromMiddle; i < 9 && i1 < size; i += 2) {
                this.setElement(row * 9 + i, elements[i1]);
                i1++;
            }
        } else {
            for (int i = 0; i < 9 && i < size; i++) {
                this.setElement(row * 9 + i, elements[i]);
            }
        }
    }

    protected void setTitle(String title) {
        this.title = title;
    }
    protected void setRows(int rows) {
        this.rows = rows;
    }

    protected void reopen() {
        MenuManager.instance.reopen(this);
    }
}
