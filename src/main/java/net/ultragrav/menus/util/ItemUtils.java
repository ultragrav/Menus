package net.ultragrav.menus.util;

import net.ultragrav.nbt.conversion.NBTConversion;
import net.ultragrav.nbt.wrapper.*;
import net.ultragrav.serializer.GravSerializer;
import net.ultragrav.serializer.JsonMeta;
import net.ultragrav.serializer.streams.GravSerializerInputStream;
import net.ultragrav.serializer.streams.GravSerializerOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemUtils {

    public static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static Class<?> clazzNBTTagCompound;
    private static Class<?> clazzItemStack;
    private static Class<?> clazzCraftItemStack;

    //Methods.
    private static Method methodAsNMSCopy;
    private static Method methodAsBukkitCopy;
    private static Method methodGetTagCompound;
    private static Method methodSetTagCompound;
    private static Method methodGetNBTByteArray;
    private static Method methodSetNBTByteArray;
    private static Method methodGetStringTag;
    private static Method methodSetStringTag;
    private static Method methodGetIntTag;
    private static Method methodSetIntTag;
    private static Method methodHasTag;
    private static Method methodRemoveTag;

    //Constructors
    private static Constructor<?> constructNBTTagCompound;

    //Initialize.
    static {
        try {
            clazzNBTTagCompound = getNMSClass("NBTTagCompound");
            clazzItemStack = getNMSClass("ItemStack");
            clazzCraftItemStack = getBukkitClass("inventory.CraftItemStack");
            methodAsNMSCopy = clazzCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            methodAsBukkitCopy = clazzCraftItemStack.getMethod("asBukkitCopy", clazzItemStack);
            methodGetTagCompound = clazzItemStack.getMethod("getTag");
            methodSetTagCompound = clazzItemStack.getMethod("setTag", clazzNBTTagCompound);
            methodGetNBTByteArray = clazzNBTTagCompound.getMethod("getByteArray", String.class);
            methodSetNBTByteArray = clazzNBTTagCompound.getMethod("setByteArray", String.class, byte[].class);
            methodGetStringTag = clazzNBTTagCompound.getMethod("getString", String.class);
            methodSetStringTag = clazzNBTTagCompound.getMethod("setString", String.class, String.class);
            methodGetIntTag = clazzNBTTagCompound.getMethod("getInt", String.class);
            methodSetIntTag = clazzNBTTagCompound.getMethod("setInt", String.class, int.class);
            methodHasTag = clazzNBTTagCompound.getMethod("hasKey", String.class);
            methodRemoveTag = clazzNBTTagCompound.getMethod("remove", String.class);

            constructNBTTagCompound = clazzNBTTagCompound.getConstructor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] toByteArray(ItemStack stack) {

        if (stack == null) return null;

        //Using bukkit's object output stream.
        try {
            GravSerializer serializer = new GravSerializer();
            BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new GravSerializerOutputStream(serializer));
            stream.writeObject(stack);
            stream.flush();
            stream.close();
            return serializer.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static ItemStack fromByteArray(byte[] arr) {
        if (arr == null) return null;
        try {
            BukkitObjectInputStream stream = new BukkitObjectInputStream(new GravSerializerInputStream(new GravSerializer(arr)));
            ItemStack stack = (ItemStack) stream.readObject();
            stream.close();
            return stack;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static int applyFortune(int drops, int level) {
        if (level == 0) return drops;
        Random random = ThreadLocalRandom.current();
        int num = random.nextInt(2 + level);
        return num < 2 ? drops : drops * num;
    }

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> getBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNMSStack(ItemStack stack) {
        try {
            return methodAsNMSCopy.invoke(null, stack);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack getBukkitStack(Object nmsStack) {
        try {
            return (ItemStack) methodAsBukkitCopy.invoke(null, nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Object getTagCompound(Object nmsStack) {
        try {
            Object tag = methodGetTagCompound.invoke(nmsStack);
            if (tag == null) {
                tag = constructNBTTagCompound.newInstance();
            }
            return tag;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setTagCompound(Object nmsStack, Object tag) {
        try {
            methodSetTagCompound.invoke(nmsStack, tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack setNBTByteArray(ItemStack stack, String tag, byte[] data) {
        //Use reflection and nms to add data to stack's NBT.
        try {
            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) {
                tagCompound = clazzNBTTagCompound.newInstance();
            }
            methodSetNBTByteArray.invoke(tagCompound, tag, data);
            methodSetTagCompound.invoke(nmsStack, tagCompound);
            return getBukkitStack(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
            return stack;
        }
    }

    //Get nbt byte array.
    public static byte[] getNBTByteArray(ItemStack stack, String tag) {
        try {
            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) return null;

            if ((Boolean) methodHasTag.invoke(tagCompound, tag)) {
                return (byte[]) methodGetNBTByteArray.invoke(tagCompound, tag);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a String tag form an item stack.
     */
    public static String getStringTag(ItemStack stack, String key) {
        try {
            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) return null;

            return (String) methodGetStringTag.invoke(tagCompound, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set a String tag for an item stack.
     */
    public static ItemStack setStringTag(ItemStack stack, String key, String value) {
        try {

            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) {
                tagCompound = clazzNBTTagCompound.newInstance();
            }
            methodSetStringTag.invoke(tagCompound, key, value);
            methodSetTagCompound.invoke(nmsStack, tagCompound);
            return getBukkitStack(nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
            return stack;
        }
    }

    public static ItemStack removeTag(ItemStack stack, String key) {
        try {
            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) return stack;

            methodRemoveTag.invoke(tagCompound, key);
            methodSetTagCompound.invoke(nmsStack, tagCompound);
            return getBukkitStack(nmsStack);

        } catch (Exception e) {
            e.printStackTrace();
            return stack;
        }
    }

    //Set/Get/Has int tags.
    public static int getIntTag(ItemStack stack, String key) {
        try {
            if (stack == null) return 0;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return 0; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) return 0;

            return (int) methodGetIntTag.invoke(tagCompound, key);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static ItemStack setIntTag(ItemStack stack, String key, int value) {
        try {
            if (stack == null) return null;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return null; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) {
                tagCompound = clazzNBTTagCompound.newInstance();
            }
            methodSetIntTag.invoke(tagCompound, key, value);
            methodSetTagCompound.invoke(nmsStack, tagCompound);
            return getBukkitStack(nmsStack);

        } catch (Exception e) {
            e.printStackTrace();
            return stack;
        }
    }

    public static boolean hasTag(ItemStack stack, String key) {
        try {
            if (stack == null) return false;
            if (stack.getType() == Material.AIR) return false;

            Object nmsStack = getNMSStack(stack);

            if (nmsStack == null) return false; //bukkit stack was Material.AIR

            Object tagCompound = getTagCompound(nmsStack);
            if (tagCompound == null) return false;

            return (boolean) methodHasTag.invoke(tagCompound, key);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JsonMeta getMetaTag(ItemStack stack, String key) {
        if (stack == null || stack.getType() == Material.AIR) return null;
        byte[] data = getNBTByteArray(stack, key);
        if (data == null) return null;
        return JsonMeta.deserialize(new GravSerializer(data));
    }

    public static ItemStack setMetaTag(ItemStack stack, String key, JsonMeta meta) {
        if (stack == null || stack.getType() == Material.AIR) return null;
        GravSerializer serializer = new GravSerializer();
        meta.serialize(serializer);
        return setNBTByteArray(stack, key, serializer.toByteArray());
    }

    /**
     * Add an attribute modifier to an item.
     */
    public static ItemStack addAttributeModifier(ItemStack stack, String attribute, int operation, double modifier, EquipmentSlot slot, String name) {

        if (stack == null || stack.getType() == Material.AIR) return stack;

        Object nmsItem = getNMSStack(stack);
        if (nmsItem == null) return stack;

        TagCompound compound = NBTConversion.wrapTag(Objects.requireNonNull(getTagCompound(nmsItem)));
        TagList list = (TagList) compound.getData().get("AttributeModifiers");
        if (list == null) {
            list = new TagList();
        }

        TagCompound modifierTag = new TagCompound();
        UUID id = UUID.randomUUID();
        modifierTag.getData().put("AttributeName", new TagString(attribute));
        modifierTag.getData().put("Name", new TagString(name));
        modifierTag.getData().put("Amount", new TagDouble(modifier));
        modifierTag.getData().put("Operation", new TagInt(operation));
        modifierTag.getData().put("Slot", new TagString(toNmsEquipmentName(slot)));
        modifierTag.getData().put("UUIDMost", new TagLong(id.getMostSignificantBits()));
        modifierTag.getData().put("UUIDLeast", new TagLong(id.getLeastSignificantBits()));

        // Remove from the list any that have the same name.
        list.getData().removeIf(tag -> {
            if (!(tag instanceof TagCompound)) return false;
            TagCompound tagCompound = (TagCompound) tag;
            if (tagCompound.getData().get("Name") instanceof TagString) {
                String nameTag = ((TagString) tagCompound.getData().get("Name")).getData();
                return nameTag.equals(name);
            }
            return false;
        });

        list.getData().add(modifierTag);
        compound.getData().put("AttributeModifiers", list);

        setTagCompound(nmsItem, NBTConversion.unwrapTag(compound, ItemUtils.version));

        return getBukkitStack(nmsItem);
    }

    public static ItemStack removeAttributeModifier(ItemStack stack, String name) {
        if (stack == null || stack.getType() == Material.AIR) return stack;

        Object nmsItem = getNMSStack(stack);
        if (nmsItem == null) return stack;

        TagCompound compound = NBTConversion.wrapTag(Objects.requireNonNull(getTagCompound(nmsItem)));
        TagList list = (TagList) compound.getData().get("AttributeModifiers");
        if (list == null) {
            list = new TagList();
        }

        for (Tag tag : list.getData()) {
            if (tag instanceof TagCompound) {
                TagCompound modifierTag = (TagCompound) tag;
                if (((TagString) modifierTag.getData().get("Name")).getData().equals(name)) {
                    list.getData().remove(modifierTag);
                    break;
                }
                compound.getData().put("AttributeModifiers", list);
            }
        }

        setTagCompound(nmsItem, NBTConversion.unwrapTag(compound, ItemUtils.version));

        return getBukkitStack(nmsItem);
    }

    public static boolean hasAttributeModifier(ItemStack item, String name) {
        if (item == null || item.getType() == Material.AIR) return false;
        Object nmsItem = getNMSStack(item);
        if (nmsItem == null) return false;
        TagCompound compound = NBTConversion.wrapTag(Objects.requireNonNull(getTagCompound(nmsItem)));
        TagList list = (TagList) compound.getData().get("AttributeModifiers");
        if (list == null) {
            return false;
        }
        for (Tag tag : list.getData()) {
            if (tag instanceof TagCompound) {
                TagCompound modifierTag = (TagCompound) tag;
                if (((TagString) modifierTag.getData().get("Name")).getData().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack clearAttributeModifiers(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return stack;

        Object nmsItem = getNMSStack(stack);
        if (nmsItem == null) return stack;

        TagCompound compound = NBTConversion.wrapTag(Objects.requireNonNull(getTagCompound(stack)));
        TagList list = (TagList) compound.getData().get("AttributeModifiers");
        if (list == null) {
            list = new TagList();
        }

        list.getData().clear();
        compound.getData().put("AttributeModifiers", list);

        setTagCompound(nmsItem, compound.getData());

        return getBukkitStack(nmsItem);
    }

    private static String toNmsEquipmentName(EquipmentSlot slot) {
        switch (slot) {
//            case OFF_HAND: TODO: Ensure this works across versions
//                return "offhand";
            case FEET:
                return "feet";
            case LEGS:
                return "legs";
            case CHEST:
                return "chest";
            case HEAD:
                return "head";
            default:
                return "mainhand";
        }
    }

    public static List<ItemStack> leatherArmor(int colour) {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);

        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();

        bootsMeta.setColor(Color.fromRGB(colour));
        leggingsMeta.setColor(Color.fromRGB(colour));
        chestplateMeta.setColor(Color.fromRGB(colour));
        helmetMeta.setColor(Color.fromRGB(colour));

        boots.setItemMeta(bootsMeta);
        leggings.setItemMeta(leggingsMeta);
        chestplate.setItemMeta(chestplateMeta);
        helmet.setItemMeta(helmetMeta);

        return Arrays.asList(boots, leggings, chestplate, helmet);

    }

    public static ItemStack removeAmount(ItemStack stack, int i) {
        if (stack == null || stack.getType() == Material.AIR) return stack;
        ItemStack item = stack.clone();

        if (item.getAmount() <= i) {
            return null;
        }

        item.setAmount(item.getAmount() - i);
        return item;
    }


    public static ItemStack setSkullTexture(ItemStack stack, String encodedTexture) {
        if (stack == null || stack.getType() != Material.SKULL_ITEM || stack.getData().getData() != 3) {
            return stack;
        }

        Object nmsStack = getNMSStack(stack);
        TagCompound tag = NBTConversion.wrapTag(Objects.requireNonNull(ItemUtils.getTagCompound(nmsStack)));
        tag.getData().put("SkullOwner", new TagCompound());
        TagCompound skullOwner = (TagCompound) tag.getData().get("SkullOwner");

        skullOwner.getData().put("Name", new TagString("abasfawsfafwsadafeasdfsdc"));
        skullOwner.getData().put("Id", new TagString(UUID.randomUUID().toString()));

        skullOwner.getData().put("Properties", new TagCompound());
        TagCompound properties = (TagCompound) skullOwner.getData().get("Properties");

        properties.getData().put("textures", new TagList());
        TagList textures = (TagList) properties.getData().get("textures");

        if (textures.getData().size() == 1) {
            textures.getData().clear();
        }
        TagCompound texture = new TagCompound();

        texture.getData().put("Value", new TagString(encodedTexture));
        textures.getData().add(texture);

        setTagCompound(nmsStack, NBTConversion.unwrapTag(tag, ItemUtils.version));

        return ItemUtils.getBukkitStack(nmsStack);
    }
}
