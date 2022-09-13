package net.ultragrav.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter @Setter
@AllArgsConstructor
public class MenuHolder implements InventoryHolder {
    private Menu menu;

    @Override
    public Inventory getInventory() {
        return null;
    }
}
