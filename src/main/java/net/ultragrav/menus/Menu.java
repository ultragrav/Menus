package net.ultragrav.menus;

import lombok.Getter;
import net.ultragrav.menus.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Menu {
    public static Material BACK_BUTTON_ITEM = Material.BARRIER;
    public static Material PAGE_CONTROL_ITEM = Material.ARROW;


    private final MenuHandlerHolder handler = new MenuHandlerHolder(this);
    private final Map<Integer, MenuElement> elements = new HashMap<>();
    private final List<UUID> viewers = new ArrayList<>();
    @Getter
    private String title;
    @Getter
    private int rows;
    private int taskId = -1;

    public Menu(String title, int rows) {
        this.title = title;
        this.rows = rows;
    }

    public Menu() {
        this("", 3);
    }

    @Deprecated
    public void build(UUID player) {
    }

    public void setup(HumanEntity player) {
        build(player.getUniqueId());
    }

    public static MenuElement getBackButton(Consumer<InventoryClickEvent> clickHandler) {
        MenuElement element = new MenuElement(new ItemBuilder(BACK_BUTTON_ITEM, 1).setName("&b&lBack").addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
        element.clickBuilder().defaultHandler(clickHandler).build();
        return element;
    }

    public static MenuElement getBackButton(Menu backMenu) {
        if (backMenu == null) {
            return getBackButton((Consumer<InventoryClickEvent>) null);
        }
        return getBackButton(e -> backMenu.open(e.getWhoClicked()));
    }

    public MenuHandlerHolder clickBuilder() {
        return handler;
    }

    public Menu setOwnInventoryClickHandler(Consumer<InventoryClickEvent> clickHandler) {
        handler.ownInventoryClickHandler().defaultHandler(clickHandler);
        return this;
    }

    public Menu setDragHandler(Consumer<InventoryDragEvent> dragHandler) {
        handler.defaultDragHandler(dragHandler).ownInventoryDragHandler(dragHandler);
        return this;
    }

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

    public void open(HumanEntity player) {
        open(player, true);
    }

    /**
     * Opens the inventory for the given player.
     *
     * @param player The player to open the inventory for.
     */
    public void open(HumanEntity player, boolean setup) {
        if (player == null) return;

        if (setup) setup(player);

        Inventory inv = createInventory();

        if (viewers.contains(player.getUniqueId())) {
            InventoryView view = player.getOpenInventory();
            if (view != null) {
                Inventory top = view.getTopInventory();
                if (top != null && top.getSize() == inv.getSize()) {
                    if (top.getHolder() instanceof MenuHolder) {
                        MenuHolder holder = (MenuHolder) top.getHolder();
                        if (holder.getMenu() == this) {
                            top.setContents(inv.getContents());
                            return;
                        }
                    }
                }
            }
        }

        if (player.getOpenInventory() != null)
            MenuManager.instance.handleClose(new InventoryCloseEvent(player.getOpenInventory()));

        viewers.add(player.getUniqueId());
        if (taskId == -1) {
            // We only do updates 10 times per second since more is unnecessary
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MenuManager.instance.plugin, this::update, 2, 2);
        }
        Bukkit.getScheduler().runTask(MenuManager.instance.plugin, () -> player.openInventory(inv));
    }

    void update() {
        List<Integer> slots = new ArrayList<>();
        for (int slot : this.elements.keySet()) {
            MenuElement e = this.elements.get(slot);
            if (e == null) {
                continue;
            }
            if (e.update()) slots.add(slot);
        }
        MenuManager.instance.invalidateElementsInInvForMenu(this, slots);

        if (getViewers().isEmpty()) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    void update(Inventory inv) {
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

    public List<UUID> getViewers() {
        viewers.removeIf(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return true;
            }
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv == null) return true;
            if (inv.getHolder() instanceof MenuHolder) {
                MenuHolder holder = (MenuHolder) inv.getHolder();
                if (holder.getMenu() != this) {
                    return true;
                }
                return false;
            } else {
                return true;
            }
        });
        return this.viewers;
    }

    public int getIndex(int row, int col) {
        return row * 9 + col;
    }

    public int getIndexByCoord(int x, int y) {
        return getIndex(y, x);
    }


    public void evenlyDistribute(int row, MenuElement... elements) {
        int size = elements.length;
        if (size == 0) return;

        if (size <= 5) {
            //Distribute separated by 2.
            int fromMiddle = size - 1;
            int i1 = 0;
            for (int i = 4 - fromMiddle; i < 9 && i1 < size; i += 2) {
                this.setElement(row * 9 + i, elements[i1]);
                i1++;
            }
        } else {
            //Distribute side by side in the middle.
            int fromMiddle = size / 2;
            int i1 = 0;
            for (int i = 4 - fromMiddle; i < 9 && i1 < size; i++) {
                this.setElement(row * 9 + i, elements[i1]);
                i1++;
            }
        }
    }

    public void fillBlack() {
        fillElement(new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName("&7").build()));
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

    public void setCol(int col, MenuElement element) {
        for (int i = col; i < this.rows * 9; i += 9) {
            this.setElement(i, element);
        }
    }

    public AtomicInteger setupActionableList(int startPos, int endPos, int backPos, int nextPos, Function<Integer, MenuElement> elementSupplier, int page) {
        return setupActionableList(startPos, endPos, backPos, nextPos, elementSupplier, new AtomicInteger(page));
    }

    public AtomicInteger setupActionableList(int startPos, int endPos, int backPos, int nextPos, Function<Integer, MenuElement> elementSupplier, AtomicInteger pageHandler) {
        //Pageable list
        int page = pageHandler.get();

        int calculatedMarginLeft = startPos % 9;
        int calculatedMarginRight = 8 - endPos % 9;

        int elementIndex = page * (9 - calculatedMarginLeft - calculatedMarginRight) * (((endPos - (endPos % 9)) / 9) - ((startPos - (startPos % 9)) / 9) + 1);
        boolean placing = true;
        for (int slot = startPos; slot <= endPos; slot++) {

            if (placing) {
                MenuElement element = elementSupplier.apply(elementIndex);
                if (element == null) {
                    placing = false;
                    this.setElement(slot, null);
                } else {
                    this.setElement(slot, element);
                }
            } else {
                this.setElement(slot, null);
            }

            if (8 - slot % 9 <= calculatedMarginRight) {
                slot += calculatedMarginLeft + calculatedMarginRight;
            }

            elementIndex++;
        }

        MenuElement back = new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&fBack").build()).clickBuilder().defaultHandler(e -> {
            pageHandler.decrementAndGet();
            this.setupActionableList(startPos, endPos, backPos, nextPos, elementSupplier, pageHandler);
            open(e.getWhoClicked(), false);
        }).build();
        MenuElement next = new MenuElement(new ItemBuilder(Material.ARROW, 1).setName("&fNext").build()).clickBuilder().defaultHandler(e -> {
            pageHandler.incrementAndGet();
            this.setupActionableList(startPos, endPos, backPos, nextPos, elementSupplier, pageHandler);
            open(e.getWhoClicked(), false);
        }).build();

        if (page != 0) {
            this.setElement(backPos, back);
        } else {
            this.setElement(backPos, null);
        }
        if (elementSupplier.apply(elementIndex) != null) {
            this.setElement(nextPos, next);
        } else {
            this.setElement(nextPos, null);
        }
        return pageHandler;
    }

    protected MenuElement getFiller(int dat) {
        return new MenuElement(new ItemBuilder(Material.STAINED_GLASS_PANE).setDurability((short) dat).setName("§7").build());
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

            if (element >= elementList.size()) break;

            setElement(index, elementList.get(element++));
        }
    }

    public void fillElement(MenuElement e) {
        for (int i = 0; i < this.rows * 9; i++) {
            if (this.getElement(i) == null) {
                this.setElement(i, e);
            }
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSize(int rows) {
        this.rows = rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getSize() {
        return rows * 9;
    }

    public int indexOfElement(MenuElement e) {
        for (int i = 0; i < this.rows * 9; i++) {
            if (e.equals(this.getElement(i))) {
                return i;
            }
        }
        return -1;
    }

    protected void reopen() {
        MenuManager.instance.reopen(this);
    }
}
