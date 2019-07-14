package me.desht.pneumaticcraft.common.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IDescSynced {
    enum Type {
        TILE_ENTITY, SEMI_BLOCK
    }

    Type getSyncType();

    List<SyncedField> getDescriptionFields();

    void writeToPacket(CompoundNBT tag);

    void readFromPacket(CompoundNBT tag);

    BlockPos getPosition();

    void onDescUpdate();
}
