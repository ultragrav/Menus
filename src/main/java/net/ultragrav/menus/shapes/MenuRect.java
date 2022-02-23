package net.ultragrav.menus.shapes;

import net.ultragrav.menus.Menu;

public class MenuRect extends MenuShape {
    private final int[] slots;

    public MenuRect(Menu menu, int minRow, int minColumn, int maxRow, int maxColumn) {
        super(menu);

        this.slots = new int[(maxColumn - minColumn + 1) * (maxRow - minRow + 1)];
        int i = 0;
        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minColumn; c <= maxColumn; c++) {
                this.slots[i++] = r * 9 + c;
            }
        }
    }

    @Override
    public int[] getSlots() {
        return this.slots;
    }
}
