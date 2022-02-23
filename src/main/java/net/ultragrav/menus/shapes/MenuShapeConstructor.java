package net.ultragrav.menus.shapes;

import net.ultragrav.menus.Menu;

import java.util.function.Function;

public class MenuShapeConstructor {
    public static Function <Menu, MenuRect> rect(int minRow, int minColumn, int maxRow, int maxColumn) {
        return menu -> new MenuRect(menu, minRow, minColumn, maxRow, maxColumn);
    }
}
