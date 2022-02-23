package net.ultragrav.menus.shapes;

import net.ultragrav.menus.Menu;
import net.ultragrav.menus.MenuElement;

import java.util.List;

public abstract class MenuShape {
    private final Menu menu;

    protected MenuShape(Menu menu) {
        this.menu = menu;
    }

    /**
     * Returns the array of slots of this shape
     *
     * @return Array of slits
     */
    public abstract int[] getSlots();

    /**
     * Fill this shape with an element
     *
     * @param element Element
     */
    public void fill(MenuElement element) {
        for (int i : getSlots()) menu.setElement(i, element);
    }

    public void fillEmpty(MenuElement element) {
        for (int i : getSlots())
            if (menu.getElement(i) == null)
                menu.setElement(i, element);
    }

    /**
     * Add these elements to the shape
     * in natural order
     *
     * @param elements Elements
     * @return Remaining elements
     */
    public int add(List<MenuElement> elements) {
        return add(elements, 0);
    }

    /**
     * Add these elements to the shape
     * in natural order with an offset
     *
     * @param elements Elements
     * @param offset   Offset
     * @return Remaining elements AFTER the last one added to the shape
     */
    public int add(List<MenuElement> elements, int offset) {
        int[] slots = getSlots();
        int size = slots.length;
        int am = Math.min(size, elements.size() - offset);

        for (int i = 0; i < am; i++) {
            menu.setElement(slots[i], elements.get(i - offset));
        }

        return elements.size() - offset - am;
    }


    /**
     * Calculate the size of this shape
     *
     * @return Size
     */
    public int size() {
        return getSlots().length;
    }
}
