package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
            PneumaticCraftUtils.serializeTank(entry.getValue(), stack, entry.getKey());
        }
        return stack;
    }

    default void deserializeTanks(NBTTagCompound tag) {
        for (Map.Entry<String,FluidTank> entry : getSerializableTanks().entrySet()) {
            entry.getValue().readFromNBT(tag.getCompoundTag(entry.getKey()));
        }
    }
}
