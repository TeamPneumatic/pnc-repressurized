package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a tile entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the tile entity when the block is placed down again.
 */
public interface ISerializableTanks {
    String SAVED_TANKS = "SavedTanks";

    /**
     * Get a mapping of all tanks; this maps a tag name, which is used as the serialization key, to a fluid tank.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<String,FluidTank> getSerializableTanks();

    @Nonnull
    default ItemStack getDroppedStack(Block b) {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(b));
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            serializeTank(entry.getValue(), stack, entry.getKey());
        }
        return stack;
    }

    default void deserializeTanks(NBTTagCompound tag) {
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            entry.getValue().readFromNBT(tag.getCompoundTag(entry.getKey()));
        }
    }


    /**
     * Serialize some tank data onto an ItemStack.  Useful to preserve tile entity tank data when breaking
     * the block.
     *
     * @param tank the fluid tank
     * @param stack the itemstack to save to
     * @param tagName name of the tag in the itemstack's NBT to store the tank data
     */
    static void serializeTank(FluidTank tank, ItemStack stack, String tagName) {
        if (tank.getFluidAmount() > 0) {
            if (!stack.hasTagCompound()) {
                stack.setTagCompound(new NBTTagCompound());
            }
            NBTTagCompound tag = stack.getTagCompound();
            if (!tag.hasKey(SAVED_TANKS, Constants.NBT.TAG_COMPOUND)) {
                tag.setTag(SAVED_TANKS, new NBTTagCompound());
            }
            NBTTagCompound subTag = tag.getCompoundTag(SAVED_TANKS);
            NBTTagCompound tankTag = new NBTTagCompound();
            tank.writeToNBT(tankTag);
            subTag.setTag(tagName, tankTag);
        }
    }

    /**
     * Deserialize some fluid tank data from an ItemStack into a fluid tank.  Useful to restore tile entity
     * tank data when placing down a block which has previously been serialized.
     *
     * @param tank the fluid tank
     * @param stack the itemstack to load from
     * @param tagName name of the tag in the itemstack's NBT which holds the saved tank data
     */
    static void deserializeTank(FluidTank tank, ItemStack stack, String tagName) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(SAVED_TANKS ,Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound subTag = stack.getTagCompound().getCompoundTag(SAVED_TANKS);
            tank.readFromNBT(subTag.getCompoundTag(tagName));
        }
    }
}
