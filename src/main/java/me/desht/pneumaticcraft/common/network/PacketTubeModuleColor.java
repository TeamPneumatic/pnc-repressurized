package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.INetworkedModule;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when logistics module colour is updated via GUI
 */
public class PacketTubeModuleColor extends LocationIntPacket {
    private final int ourColor;
    private final Direction side;

    public PacketTubeModuleColor(TubeModule module) {
        super(module.getTube().getBlockPos());

        this.ourColor = ((INetworkedModule) module).getColorChannel();
        this.side = module.getDirection();
    }

    PacketTubeModuleColor(PacketBuffer buffer) {
        super(buffer);

        this.ourColor = buffer.readByte();
        this.side = Direction.from3DDataValue(buffer.readByte());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);

        buf.writeByte(ourColor);
        buf.writeByte(side.get3DDataValue());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                PneumaticCraftUtils.getTileEntityAt(player.level, pos, TileEntityPressureTube.class).ifPresent(te -> {
                    TubeModule module = te.getModule(side);
                    if (module instanceof INetworkedModule) {
                        ((INetworkedModule) module).setColorChannel(ourColor);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
