package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketUpdatePressureModule extends PacketUpdateTubeModule<PacketUpdatePressureModule> {
    private int fieldId;
    private float value;

    public PacketUpdatePressureModule() {
    }

    public PacketUpdatePressureModule(TubeModule module, int fieldId, float value) {
        super(module);
        this.fieldId = fieldId;
        this.value = value;
    }

    public PacketUpdatePressureModule(PacketBuffer buffer) {
        super(buffer);
        this.fieldId = buffer.readInt();
        this.value = buffer.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(fieldId);
        buffer.writeFloat(value);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, PlayerEntity player) {
        if (fieldId == 0) {
            module.lowerBound = value;
        } else if (fieldId == 1) {
            module.higherBound = value;
        } else if (fieldId == 2) {
            module.advancedConfig = value > 0.5F;
        }
        if (!player.world.isRemote) {
            module.sendDescriptionPacket();
        }
    }
}
