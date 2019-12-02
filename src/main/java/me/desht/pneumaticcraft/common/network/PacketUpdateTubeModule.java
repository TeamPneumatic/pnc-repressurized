package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when tube module settings are updated via GUI.
 */
public abstract class PacketUpdateTubeModule extends LocationIntPacket {

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
        buffer.writeByte((byte) moduleSide.getIndex());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ctx.get().getSender().getServerWorld().getTileEntity(pos);
            if (te instanceof TileEntityPressureTube) {
                TubeModule module = ((TileEntityPressureTube)te).modules[moduleSide.getIndex()];
                if (module != null) {
                    PlayerEntity player = ctx.get().getSender();
                    onModuleUpdate(module, player);
//                    if (player != null && !player.world.isRemote) NetworkHandler.sendToAllAround(this, player.world);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    protected abstract void onModuleUpdate(TubeModule module, PlayerEntity player);

}
