package net.ultragrav.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter
@AllArgsConstructor
public class MenuHolder implements InventoryHolder {
    private Menu menu;

    @Override
    public Inventory getInventory() {
        return null;
    }
}
