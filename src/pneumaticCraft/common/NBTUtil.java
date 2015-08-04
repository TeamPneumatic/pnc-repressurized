package pneumaticCraft.common;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class NBTUtil{
    /**
     * Initializes the {@link NBTTagCompound} for the given {@link ItemStack} if
     * it is null.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     */
    public static void initNBTTagCompound(ItemStack itemStack){
        if(itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
    }

    /**
     * Checks if the {@link NBTTagCompound} of the given {@link ItemStack} has a
     * given tag.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be checked.
     * @return True if the {@link NBTTagCompound} has the tag otherwise false.
     */
    public static boolean hasTag(ItemStack itemStack, String tagName){
        if(itemStack.stackTagCompound != null) {
            return itemStack.stackTagCompound.hasKey(tagName);
        }
        return false;
    }

    /**
     * Removes the given tag from the {@link NBTTagCompound} of the given
     * {@link ItemStack}.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be removed.
     */
    public static void removeTag(ItemStack itemStack, String tagName){
        if(itemStack.stackTagCompound != null) {
            itemStack.stackTagCompound.removeTag(tagName);
        }
    }

    /**
     * Gets a String value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a String or an empty String if the
     *         NBT Tag Compound has no such key.
     */
    public static String getString(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setString(itemStack, tagName, "");
        }

        return itemStack.stackTagCompound.getString(tagName);
    }

    /**
     * Sets the given String value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a String.
     */
    public static void setString(ItemStack itemStack, String tagName, String tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setString(tagName, tagValue);
    }

    /**
     * Gets a Boolean value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Boolean or false if the NBT Tag
     *         Compound has no such key.
     */
    public static boolean getBoolean(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setBoolean(itemStack, tagName, false);
        }

        return itemStack.stackTagCompound.getBoolean(tagName);
    }

    /**
     * Sets the given boolean value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a boolean.
     */
    public static void setBoolean(ItemStack itemStack, String tagName, boolean tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setBoolean(tagName, tagValue);
    }

    /**
     * Gets a Byte value of the given tag from the {@link NBTTagCompound} of the
     * given {@link ItemStack}. If the {@link NBTTagCompound} is null it will be
     * initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Byte or 0 if the NBT Tag Compound
     *         has no such key.
     */
    public static byte getByte(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setByte(itemStack, tagName, (byte)0);
        }

        return itemStack.stackTagCompound.getByte(tagName);
    }

    /**
     * Sets the given byte value for the given tag on the {@link NBTTagCompound}
     * of the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a byte.
     */
    public static void setByte(ItemStack itemStack, String tagName, byte tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setByte(tagName, tagValue);
    }

    /**
     * Gets a Short value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Short or 0 if the NBT Tag
     *         Compound has no such key.
     */
    public static short getShort(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setShort(itemStack, tagName, (short)0);
        }

        return itemStack.stackTagCompound.getShort(tagName);
    }

    /**
     * Sets the given short value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a short.
     */
    public static void setShort(ItemStack itemStack, String tagName, short tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setShort(tagName, tagValue);
    }

    /**
     * Gets an Integer value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as an Integer or 0 if the NBT Tag
     *         Compound has no such key.
     */
    public static int getInteger(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setInteger(itemStack, tagName, 0);
        }

        return itemStack.stackTagCompound.getInteger(tagName);
    }

    /**
     * Sets the given integer value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as an integer.
     */
    public static void setInteger(ItemStack itemStack, String tagName, int tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setInteger(tagName, tagValue);
    }

    /**
     * Gets a Long value of the given tag from the {@link NBTTagCompound} of the
     * given {@link ItemStack}. If the {@link NBTTagCompound} is null it will be
     * initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Long or 0 if the NBT Tag Compound
     *         has no such key.
     */
    public static long getLong(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setLong(itemStack, tagName, 0);
        }

        return itemStack.stackTagCompound.getLong(tagName);
    }

    /**
     * Sets the given long value for the given tag on the {@link NBTTagCompound}
     * of the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a long.
     */
    public static void setLong(ItemStack itemStack, String tagName, long tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setLong(tagName, tagValue);
    }

    /**
     * Gets a Float value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Float or 0 if the NBT Tag
     *         Compound has no such key.
     */
    public static float getFloat(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setFloat(itemStack, tagName, 0);
        }

        return itemStack.stackTagCompound.getFloat(tagName);
    }

    /**
     * Sets the given float value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a float.
     */
    public static void setFloat(ItemStack itemStack, String tagName, float tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setFloat(tagName, tagValue);
    }

    /**
     * Gets a Double value of the given tag from the {@link NBTTagCompound} of
     * the given {@link ItemStack}. If the {@link NBTTagCompound} is null it
     * will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a Double or 0 if the NBT Tag
     *         Compound has no such key.
     */
    public static double getDouble(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            setDouble(itemStack, tagName, 0);
        }

        return itemStack.stackTagCompound.getDouble(tagName);
    }

    /**
     * Sets the given double value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a double.
     */
    public static void setDouble(ItemStack itemStack, String tagName, double tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setDouble(tagName, tagValue);
    }

    /**
     * Gets a {@link NBTTagCompound} value of the given tag from the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag for which the value should be determined.
     * @return The value of the given tag as a {@link NBTTagCompound}.
     */
    public static NBTTagCompound getCompoundTag(ItemStack itemStack, String tagName){
        initNBTTagCompound(itemStack);

        if(!itemStack.stackTagCompound.hasKey(tagName)) {
            itemStack.stackTagCompound.setTag(tagName, new NBTTagCompound());
        }

        return itemStack.stackTagCompound.getCompoundTag(tagName);
    }

    /**
     * Sets the given {@link NBTTagCompound} value for the given tag on the
     * {@link NBTTagCompound} of the given {@link ItemStack}. If the
     * {@link NBTTagCompound} is null it will be initialized.
     * 
     * @param itemStack
     *            The {@link ItemStack} which holds the {@link NBTTagCompound}.
     * @param tagName
     *            The name of the tag which should be set.
     * @param tagValue
     *            The value which should be set to the given tag as a
     *            {@link NBTTagCompound}.
     */
    public static void setCompoundTag(ItemStack itemStack, String tagName, NBTBase tagValue){
        initNBTTagCompound(itemStack);
        itemStack.stackTagCompound.setTag(tagName, tagValue);
    }
}
