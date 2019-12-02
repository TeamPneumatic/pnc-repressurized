package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class PacketUpdatePressureModule extends PacketUpdateTubeModule {

    private float lower;
    private float higher;
    private boolean advanced;

    public PacketUpdatePressureModule() {
    }

    public PacketUpdatePressureModule(TubeModule module) {
        super(module);
        this.lower = module.lowerBound;
        this.higher = module.higherBound;
        this.advanced = module.advancedConfig;
    }

    public PacketUpdatePressureModule(PacketBuffer buffer) {
        super(buffer);
        this.lower = buffer.readFloat();
        this.higher = buffer.readFloat();
        this.advanced = buffer.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeFloat(lower);
        buffer.writeFloat(higher);
        buffer.writeBoolean(advanced);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, PlayerEntity player) {
        module.lowerBound = lower;
        module.higherBound = higher;
        module.advancedConfig = advanced;
        module.sendDescriptionPacket();
    }
}
