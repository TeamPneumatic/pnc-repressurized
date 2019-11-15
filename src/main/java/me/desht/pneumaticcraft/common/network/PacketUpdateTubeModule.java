package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 */
public abstract class PacketUpdateTubeModule<REQ extends PacketUpdateTubeModule<REQ>> extends LocationIntPacket {

    protected Direction moduleSide;

    public PacketUpdateTubeModule() {
    }

    public PacketUpdateTubeModule(TubeModule module) {
        super(module.getTube().pos());
        this.moduleSide = module.getDirection();
    }

    public PacketUpdateTubeModule(PacketBuffer buffer) {
        super(buffer);
        this.moduleSide = Direction.byIndex(buffer.readByte());
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeByte((byte) moduleSide.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntityPressureTube te = TileEntityPressureTube.getTube(getTileEntity(ctx));
            if (te != null) {
                TubeModule module = te.modules[moduleSide.getIndex()];
                if (module != null) {
                    ServerPlayerEntity player = ctx.get().getSender();
                    onModuleUpdate(module, player);
                    if (!player.world.isRemote) NetworkHandler.sendToAllAround(this, player.world);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void onModuleUpdate(TubeModule module, PlayerEntity player);

}
