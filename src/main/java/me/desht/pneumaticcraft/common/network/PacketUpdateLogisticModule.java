package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when the status or colour of a logistic module is updated
 */
public class PacketUpdateLogisticModule extends LocationIntPacket {

    private int side;
    private int colorIndex;
    private int status;

    public PacketUpdateLogisticModule() {
    }

    public PacketUpdateLogisticModule(ModuleLogistics logisticsModule, int action) {
        super(logisticsModule.getTube().pos());
        side = logisticsModule.getDirection().ordinal();
        colorIndex = logisticsModule.getColorChannel();
        if (action > 0) {
            status = 1 + action;
        } else {
            status = logisticsModule.hasPower() ? 1 : 0;
        }
    }

    public PacketUpdateLogisticModule(PacketBuffer buffer) {
        super(buffer);
        side = buffer.readByte();
        colorIndex = buffer.readByte();
        status = buffer.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(side);
        buf.writeByte(colorIndex);
        buf.writeByte(status);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntityPressureTube te = TileEntityPressureTube.getTube(getTileEntity(ctx));
            if (te != null) {
                TubeModule module = te.modules[side];
                if (module instanceof ModuleLogistics) {
                    ((ModuleLogistics) module).onUpdatePacket(status, colorIndex);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
