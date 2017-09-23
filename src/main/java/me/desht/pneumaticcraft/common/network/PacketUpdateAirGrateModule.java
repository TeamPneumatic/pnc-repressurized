package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.tubes.ModuleAirGrate;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketUpdateAirGrateModule extends PacketUpdateTubeModule<PacketUpdateAirGrateModule> {
    private String entityFilter;

    public PacketUpdateAirGrateModule() {
    }

    public PacketUpdateAirGrateModule(TubeModule module, String entityFilter) {
        super(module);
        this.entityFilter = entityFilter;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, entityFilter);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        entityFilter = ByteBufUtils.readUTF8String(buffer);
    }

    @Override
    protected void onModuleUpdate(TubeModule module, PacketUpdateAirGrateModule message, EntityPlayer player) {
        if (module instanceof ModuleAirGrate) {
            ((ModuleAirGrate) module).entityFilter = message.entityFilter;
        }
    }
}
