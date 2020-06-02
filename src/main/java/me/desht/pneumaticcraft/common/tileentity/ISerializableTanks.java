package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a tile entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the tile entity when the block is placed down again.
 */
public interface ISerializableTanks {

    /**
     * Get a mapping of all tanks; this maps a tag name, which is used as the serialization key, to a fluid tank.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<String, FluidTank> getSerializableTanks();

    default void deserializeTanks(CompoundNBT tag) {
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            entry.getValue().readFromNBT(tag.getCompound(entry.getKey()));
        }
    }

    default CompoundNBT serializeTanks() {
        CompoundNBT tag = new CompoundNBT();
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            if (!entry.getValue().getFluid().isEmpty()) {
                tag.put(entry.getKey(), entry.getValue().writeToNBT(new CompoundNBT()));
            }
        }
        return tag;
    }

    /**
     * Serialize some tank data onto an ItemStack.  Useful to preserve tile entity tank data when breaking
     * the block, or when using the item's fluid handler capability.  If the tank is empty, it will be removed
     * from the stack's NBT.
     *
     * @param tank the fluid tank
     * @param stack the itemstack to save to
     * @param tagName name of the subtag in the itemstack's NBT to store the tank data
     */
     static void serializeTank(FluidTank tank, ItemStack stack, String tagName) {
         CompoundNBT tag = stack.getOrCreateChildTag(NBTKeys.BLOCK_ENTITY_TAG);
         CompoundNBT subTag = tag.getCompound(NBTKeys.NBT_SAVED_TANKS);
         if (!tank.getFluid().isEmpty()) {
             subTag.put(tagName, tank.writeToNBT(new CompoundNBT()));
         } else {
             subTag.remove(tagName);
         }
         if (!subTag.isEmpty()) {
             tag.put(NBTKeys.NBT_SAVED_TANKS, subTag);
         } else {
             tag.remove(NBTKeys.NBT_SAVED_TANKS);
             if (tag.isEmpty()) {
                 stack.getTag().remove(NBTKeys.BLOCK_ENTITY_TAG);
             }
         }
    }

    /**
     * Deserialize some fluid tank data from an ItemStack into a fluid tank.  Useful when using the
     * item's fluid handler capability.
     *
     * @param stack the itemstack to load from
     * @param tagName name of the subtag in the itemstack's NBT which holds the saved tank data
     * @param capacity capacity of the created tank
     * @return the deserialized tank, or null
     */
    static FluidTank deserializeTank(ItemStack stack, String tagName, int capacity) {
        CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains(NBTKeys.NBT_SAVED_TANKS)) {
            FluidTank tank = new FluidTank(capacity);
            CompoundNBT subTag = tag.getCompound(NBTKeys.NBT_SAVED_TANKS);
            return tank.readFromNBT(subTag.getCompound(tagName));
        }
        return null;
    }
}
