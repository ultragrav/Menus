package net.ultragrav.menus;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
public class ElementUpdater {
    protected MenuElement element;

    public ElementUpdater setName(String str) {
        ItemMeta meta = element.getItem().getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', str));
        element.getItem().setItemMeta(meta);
        return this;
    }

    /**
     * Set a lore line, note the lore must already have sufficient lines for this to work
     *
     * @param line Line number
     * @param str  New text on line
     * @return {@code this}
     */
    public ElementUpdater setLoreLine(int line, String str) {
        ItemMeta meta = element.getItem().getItemMeta();
        List<String> lore = meta.getLore();
        lore.set(line, str);
        meta.setLore(lore);
        element.getItem().setItemMeta(meta);
        return this;
    }

    public ElementUpdater set(ItemStack item) {
        element.setItem(item);
        return this;
    }

    public ElementUpdater apply(Consumer<MenuElement> consumer) {
        consumer.accept(element);
        return this;
    }
}
