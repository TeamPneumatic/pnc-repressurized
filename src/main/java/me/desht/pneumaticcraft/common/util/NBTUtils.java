/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;

import java.util.Optional;

public class NBTUtils {
//    /**
//     * Initializes the {@link CompoundTag} for the given {@link ItemStack} if
//     * it is null.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     */
//    public static void initNBTTagCompound(ItemStack itemStack) {
//        if (!itemStack.hasTag()) {
//            itemStack.setTag(new CompoundTag());
//        }
//    }
//
//    /**
//     * Checks if the {@link CompoundTag} of the given {@link ItemStack} has a
//     * given tag.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be checked.
//     * @return True if the {@link CompoundTag} has the tag otherwise false.
//     */
//    public static boolean hasTag(ItemStack itemStack, String tagName) {
//        return itemStack.getTag() != null && itemStack.getTag().contains(tagName);
//    }
//
//    /**
//     * Removes the given tag from the {@link CompoundTag} of the given
//     * {@link ItemStack}.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be removed.
//     */
//    public static void removeTag(ItemStack itemStack, String tagName) {
//        if (itemStack.getTag() != null) {
//            itemStack.getTag().remove(tagName);
//        }
//    }
//
//    /**
//     * Gets a String value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a String or an empty String if the
//     * NBT Tag Compound has no such key.
//     */
//    public static String getString(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setString(itemStack, tagName, "");
//        }
//
//        return itemStack.getTag().getString(tagName);
//    }
//
//    /**
//     * Sets the given String value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a String.
//     */
//    public static void setString(ItemStack itemStack, String tagName, String tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putString(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Boolean value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Boolean or false if the NBT Tag
//     * Compound has no such key.
//     */
//    public static boolean getBoolean(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setBoolean(itemStack, tagName, false);
//        }
//
//        return itemStack.getTag().getBoolean(tagName);
//    }
//
//    /**
//     * Sets the given boolean value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a boolean.
//     */
//    public static void setBoolean(ItemStack itemStack, String tagName, boolean tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putBoolean(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Byte value of the given tag from the {@link CompoundTag} of the
//     * given {@link ItemStack}. If the {@link CompoundTag} is null it will be
//     * initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Byte or 0 if the NBT Tag Compound
//     * has no such key.
//     */
//    public static byte getByte(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setByte(itemStack, tagName, (byte) 0);
//        }
//
//        return itemStack.getTag().getByte(tagName);
//    }
//
//    /**
//     * Sets the given byte value for the given tag on the {@link CompoundTag}
//     * of the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a byte.
//     */
//    public static void setByte(ItemStack itemStack, String tagName, byte tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putByte(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Short value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Short or 0 if the NBT Tag
//     * Compound has no such key.
//     */
//    public static short getShort(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setShort(itemStack, tagName, (short) 0);
//        }
//
//        return itemStack.getTag().getShort(tagName);
//    }
//
//    /**
//     * Sets the given short value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a short.
//     */
//    public static void setShort(ItemStack itemStack, String tagName, short tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putShort(tagName, tagValue);
//    }
//
//    /**
//     * Gets an Integer value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as an Integer or 0 if the NBT Tag
//     * Compound has no such key.
//     */
//    public static int getInteger(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setInteger(itemStack, tagName, 0);
//        }
//
//        return itemStack.getTag().getInt(tagName);
//    }
//
//    /**
//     * Sets the given integer value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as an integer.
//     */
//    public static void setInteger(ItemStack itemStack, String tagName, int tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putInt(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Long value of the given tag from the {@link CompoundTag} of the
//     * given {@link ItemStack}. If the {@link CompoundTag} is null it will be
//     * initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Long or 0 if the NBT Tag Compound
//     * has no such key.
//     */
//    public static long getLong(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setLong(itemStack, tagName, 0);
//        }
//
//        return itemStack.getTag().getLong(tagName);
//    }
//
//    /**
//     * Sets the given long value for the given tag on the {@link CompoundTag}
//     * of the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a long.
//     */
//    public static void setLong(ItemStack itemStack, String tagName, long tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putLong(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Float value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Float or 0 if the NBT Tag
//     * Compound has no such key.
//     */
//    public static float getFloat(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setFloat(itemStack, tagName, 0);
//        }
//
//        return itemStack.getTag().getFloat(tagName);
//    }
//
//    /**
//     * Sets the given float value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a float.
//     */
//    public static void setFloat(ItemStack itemStack, String tagName, float tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putFloat(tagName, tagValue);
//    }
//
//    /**
//     * Gets a Double value of the given tag from the {@link CompoundTag} of
//     * the given {@link ItemStack}. If the {@link CompoundTag} is null it
//     * will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a Double or 0 if the NBT Tag
//     * Compound has no such key.
//     */
//    public static double getDouble(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            setDouble(itemStack, tagName, 0);
//        }
//
//        return itemStack.getTag().getDouble(tagName);
//    }
//
//    /**
//     * Sets the given double value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a double.
//     */
//    public static void setDouble(ItemStack itemStack, String tagName, double tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().putDouble(tagName, tagValue);
//    }
//
//    /**
//     * Gets a {@link CompoundTag} value of the given tag from the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag for which the value should be determined.
//     * @return The value of the given tag as a {@link CompoundTag}.
//     */
//    public static CompoundTag getCompoundTag(ItemStack itemStack, String tagName) {
//        initNBTTagCompound(itemStack);
//
//        if (!itemStack.getTag().contains(tagName)) {
//            itemStack.getTag().put(tagName, new CompoundTag());
//        }
//
//        return itemStack.getTag().getCompound(tagName);
//    }
//
//    /**
//     * Sets the given {@link CompoundTag} value for the given tag on the
//     * {@link CompoundTag} of the given {@link ItemStack}. If the
//     * {@link CompoundTag} is null it will be initialized.
//     *
//     * @param itemStack The {@link ItemStack} which holds the {@link CompoundTag}.
//     * @param tagName   The name of the tag which should be set.
//     * @param tagValue  The value which should be set to the given tag as a
//     *                  {@link CompoundTag}.
//     */
//    public static void setCompoundTag(ItemStack itemStack, String tagName, Tag tagValue) {
//        initNBTTagCompound(itemStack);
//        itemStack.getTag().put(tagName, tagValue);
//    }
//
//    public static void setPos(ItemStack stack, Vec3i vec) {
//        initNBTTagCompound(stack);
//        setPos(stack.getTag(), vec);
//    }
//
//    public static BlockPos getPos(ItemStack stack) {
//        initNBTTagCompound(stack);
//        return getPos(stack.getTag());
//    }

    public static CompoundTag setPos(CompoundTag tag, Vec3i vec) {
        tag.putInt("x", vec.getX());
        tag.putInt("y", vec.getY());
        tag.putInt("z", vec.getZ());
        return tag;
    }

    public static Optional<BlockPos> getPos(int[] array) {
        return array.length == 3 ? Optional.of(new BlockPos(array[0], array[1], array[2])) : Optional.empty();
    }

//    public static BlockPos getPos(IntArrayTag tag) {
//        return new BlockPos(tag.get(0).getAsInt(), tag.get(1).getAsInt(), tag.get(2).getAsInt());
//    }

    public static boolean fromTag(CompoundTag tag, String name, boolean def) {
        return tag.contains(name) ? tag.getBoolean(name) : def;
    }

    public static int fromTag(CompoundTag tag, String name, int def) {
        return tag.contains(name) ? tag.getInt(name) : def;
    }

    public static String fromTag(CompoundTag tag, String name, String def) {
        return tag.contains(name) ? tag.getString(name) : def;
    }

    public static float fromTag(CompoundTag tag, String name, float def) {
        return tag.contains(name) ? tag.getFloat(name) : def;
    }

//    public static ListTag serializeTextComponents(List<Component> textComponents) {
//        ListTag l = new ListTag();
//        textComponents.forEach(t -> l.add(StringTag.valueOf(Component.Serializer.toJson(t))));
//        return l;
//    }
//
//    public static List<Component> deserializeTextComponents(ListTag l) {
//        return l.stream()
//                .filter(nbt -> nbt instanceof StringTag)
//                .map(nbt -> Component.Serializer.fromJson(nbt.getAsString()))  // fromJsonStrict
//                .collect(Collectors.toList());
//    }
}
