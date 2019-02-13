package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a tile entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the tile entity when the block is placed down again.
 */
public interface ISerializableTanks {
    String NBT_SAVED_TANKS = "SavedTanks";

    /**
     * Get a mapping of all tanks; this maps a tag name, which is used as the serialization key, to a fluid tank.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<String,FluidTank> getSerializableTanks();

    default void deserializeTanks(NBTTagCompound tag) {
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            entry.getValue().readFromNBT(tag.getCompoundTag(entry.getKey()));
        }
    }

    default void serializeTanks(ItemStack customDrop) {
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            serializeTank(entry.getValue(), customDrop, entry.getKey());
        }
    }

    /**
     * Serialize some tank data onto an ItemStack.  Useful to preserve tile entity tank data when breaking
     * the block, or when using the item's fluid handler capability.  If the tank is empty, it will not be
     * serialized at all.
     *
     * @param tank the fluid tank
     * @param stack the itemstack to save to
     * @param tagName name of the subtag in the itemstack's NBT to store the tank data
     */
     static void serializeTank(FluidTank tank, ItemStack stack, String tagName) {
        if (tank.getFluid() != null && tank.getFluid().amount > 0) {
            NBTTagCompound subTag = NBTUtil.getCompoundTag(stack, NBT_SAVED_TANKS);
            subTag.setTag(tagName, tank.writeToNBT(new NBTTagCompound()));
            NBTUtil.setCompoundTag(stack, NBT_SAVED_TANKS, subTag);
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
        if (NBTUtil.hasTag(stack, NBT_SAVED_TANKS)) {
            FluidTank tank = new FluidTank(capacity);
            NBTTagCompound subTag = NBTUtil.getCompoundTag(stack, NBT_SAVED_TANKS);
            return tank.readFromNBT(subTag.getCompoundTag(tagName));
        }
        return null;
    }
}
