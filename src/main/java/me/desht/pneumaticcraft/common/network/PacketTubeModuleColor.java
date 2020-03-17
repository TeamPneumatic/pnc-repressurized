package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.INetworkedModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public class PacketTubeModuleColor extends LocationIntPacket {
    private int ourColor;
    private Direction side;

    public PacketTubeModuleColor() {
    }

    public PacketTubeModuleColor(TubeModule module) {
        super(module.getTube().getPos());

        this.ourColor = ((INetworkedModule) module).getColorChannel();
        this.side = module.getDirection();
    }

    PacketTubeModuleColor(PacketBuffer buffer) {
        super(buffer);

        this.ourColor = buffer.readByte();
        this.side = Direction.byIndex(buffer.readByte());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);

        buf.writeByte(ourColor);
        buf.writeByte(side.getIndex());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntityPressureTube te = TileEntityPressureTube.getTube(ctx.get().getSender().getServerWorld().getTileEntity(pos));
            if (te != null) {
                TubeModule module = te.modules[side.getIndex()];
                if (module instanceof INetworkedModule) {
                    ((INetworkedModule) module).setColorChannel(ourColor);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
