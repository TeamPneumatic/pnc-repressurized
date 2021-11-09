package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.util.PNCFluidTank;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents a tile entity which stores tank data that should be serialized onto the dropped item stack when the block
 * is broken, and deserialized back to the tile entity when the block is placed down again.
 */
@FunctionalInterface
public interface ISerializableTanks {
    /**
     * Get a mapping of all tanks; this maps a tag name, which is used as the serialization key, to a fluid tank.
     *
     * @return a map of tag names to tanks
     */
    @Nonnull
    Map<String, PNCFluidTank> getSerializableTanks();

    default void deserializeTanks(CompoundNBT tag) {
        getSerializableTanks().forEach((key, tank) -> tank.readFromNBT(tag.getCompound(key)));
    }

    default CompoundNBT serializeTanks() {
        CompoundNBT tag = new CompoundNBT();
        getSerializableTanks().forEach((key, tank) -> {
            if (!tank.getFluid().isEmpty()) {
                tag.put(key, tank.writeToNBT(new CompoundNBT()));
            }
        });
        return tag;
    }
}
