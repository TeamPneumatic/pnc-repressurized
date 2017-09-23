package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.inventory.SyncedField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IDescSynced {
    enum Type {
        TILE_ENTITY, SEMI_BLOCK
    }

    Type getSyncType();

    List<SyncedField> getDescriptionFields();

    void writeToPacket(NBTTagCompound tag);

    void readFromPacket(NBTTagCompound tag);

    BlockPos getPosition();

    void onDescUpdate();
}
