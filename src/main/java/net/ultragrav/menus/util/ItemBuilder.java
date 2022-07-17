package net.ultragrav.menus.util;

import com.google.common.collect.Lists;
import net.ultragrav.nbt.conversion.NBTConversion;
import net.ultragrav.nbt.wrapper.TagCompound;
import net.ultragrav.nbt.wrapper.TagList;
import net.ultragrav.nbt.wrapper.TagString;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ItemBuilder {
    private ItemStack item;

    public ItemBuilder(Material m, int amount, int data) {
        this(m, amount, (byte) data);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    public ItemBuilder(Material m, int amount) {
        if (m == null) {
            this.item = null;
            return;
        }
        this.item = new ItemStack(m, amount);
    }

    public ItemBuilder(Material m, int amount, byte data) {
        if (m == null) {
            this.item = null;
            return;
        }
        this.item = new ItemStack(m, amount, data);
    }

    public ItemBuilder(Material m) {
        this(m, 1);
    }

    public ItemBuilder setDurability(short val) {
        this.item.setDurability(val);
        return this;
    }

    public ItemBuilder setDurabilityLeft(short val) {
        this.item.setDurability((short) (this.item.getType().getMaxDurability() - val + 1));
        return this;
    }

    public ArrayList<String> getLore() {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        return Lists.newArrayList(lore);
    }

    /**
     * Apply a function to the lore of the item.
     *
     * @param function - The function to apply to the lore.
     * @return {@code this}
     */
    public ItemBuilder replaceLore(Function<List<String>, List<String>> function) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        meta.setLore(function.apply(lore));
        item.setItemMeta(meta);
        return this;
    }

    private static volatile Method getNmsCopyMethod;
    private static volatile Method getNameMethod;

    public String getName() {
        String name = item.getItemMeta() == null ? null : item.getItemMeta().getDisplayName();
        if (name == null) {
            name = "";
            if (getNmsCopyMethod == null) {
                try {
                    String clazz = Bukkit.getServer().getClass().getName();
                    clazz = clazz.substring(0, clazz.lastIndexOf("."));
                    clazz += ".inventory.CraftItemStack";

                    getNmsCopyMethod = Class.forName(clazz).getDeclaredMethod("asNMSCopy", ItemStack.class);
                } catch (NoSuchMethodException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                Object nmsItem = getNmsCopyMethod.invoke(null, item);
                if (getNameMethod == null) {
                    getNameMethod = nmsItem.getClass().getMethod("getName");
                }
                name = (String) getNameMethod.invoke(nmsItem);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
        return name;
    }

    public ItemBuilder setupAsSkull(OfflinePlayer owner) {
        this.setType(Material.SKULL_ITEM);
        this.setDurability((short) 3);
        this.setSkullOwner(owner);
        return this;
    }

    public ItemBuilder setupAsSkull(String textureData) {
        Object nms = ItemUtils.getNMSStack(this.item);
        Object tag = ItemUtils.getTagCompound(nms);
        TagCompound wrappedTag = tag == null ? new TagCompound() : NBTConversion.wrapTag(tag);
        TagCompound skullOwner = new TagCompound();
        wrappedTag.getData().put("SkullOwner", skullOwner);
        skullOwner.getData().put("Id", new TagString(UUID.randomUUID().toString()));
        skullOwner.getData().put("Name", new TagString("HELLO_WORLD_IS_MY_NAME"));
        TagCompound properties = new TagCompound();
        skullOwner.getData().put("Properties", properties);
        TagList textures = new TagList();
        properties.getData().put("textures", textures);
        TagCompound texture = new TagCompound();
        textures.getData().add(texture);
        texture.getData().put("Value", new TagString(textureData));

        tag = NBTConversion.unwrapTag(wrappedTag, ItemUtils.version);
        ItemUtils.setTagCompound(nms, tag);
        this.item = ItemUtils.getBukkitStack(nms);
        return this;
    }

    private ItemBuilder setType(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        ItemMeta meta = this.item.getItemMeta();
        meta.addItemFlags(flags);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeItemFlags(ItemFlag... flags) {
        ItemMeta meta = this.item.getItemMeta();
        meta.removeItemFlags(flags);
        this.item.setItemMeta(meta);
        return this;
    }

    public boolean hasItemFlag(ItemFlag flag) {
        return this.item.getItemMeta().hasItemFlag(flag);
    }

    public ItemMeta getItemMeta() {
        return this.item.getItemMeta();
    }

    public ItemBuilder setUnbreakable(boolean bool) {
        ItemMeta meta = this.item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullOwner(OfflinePlayer owner) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner.getName());
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setSkullOwner(String ownerName) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(ownerName);
            this.item.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addLore(String s) {
        if (s == null) return this;
        return addLore(s, true);
    }

    public ItemBuilder addLore(String... s) {
        if (s == null) return this;
        for (String str : s) {
            addLore(str);
        }
        return this;
    }

    public ItemBuilder addLore(List<String> s) {
        if (s == null) return this;
        for (String str : s) {
            addLore(str);
        }
        return this;
    }

    public ItemBuilder addLore(String s, boolean translateColours) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return this;
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(translateColours ? ChatColor.translateAlternateColorCodes('&', s) : s);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(int index, String s) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return this;
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(index, ChatColor.translateAlternateColorCodes('&', s));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLore(String lore, boolean removeAll) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore1 = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        while (lore1.contains(ChatColor.translateAlternateColorCodes('&', lore))) {
            lore1.remove(ChatColor.translateAlternateColorCodes('&', lore));
            if (!removeAll) {
                break;
            }
        }
        meta.setLore(lore1);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLore(int index) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore1 = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        if (index >= 0 && index < lore1.size()) {
            lore1.remove(index);
        }
        meta.setLore(lore1);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        if (level == 0) return removeEnchantment(enchantment);
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        this.item.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder setLeatherColour(int color) {
        if (/*!this.item.hasItemMeta() || */!(this.item.getItemMeta() instanceof LeatherArmorMeta)) {
            return this;
        }

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setColor(Color.fromRGB(color));
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(int i, String s) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        while (lore.size() <= i) {
            lore.add("");
        }
        lore.set(i, ChatColor.translateAlternateColorCodes('&', s));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setName(String s) {
        if (s == null) return this;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', s));
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return this.item;
    }
}
