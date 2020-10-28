package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when tube module settings are updated via GUI.
 */
public abstract class PacketUpdateTubeModule extends LocationIntPacket {
    private Direction moduleSide;

    public PacketUpdateTubeModule() {
    }

    public PacketUpdateTubeModule(TubeModule module) {
        super(module.getTube().getPos());
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
        ctx.get().enqueueWork(() -> PneumaticCraftUtils.getTileEntityAt(ctx.get().getSender().getServerWorld(), pos, TileEntityPressureTube.class).ifPresent(te -> {
            TubeModule tm = te.getModule(moduleSide);
            if (tm != null) {
                PlayerEntity player = ctx.get().getSender();
                onModuleUpdate(tm, player);
            }
        }));
        ctx.get().setPacketHandled(true);
    }

    protected abstract void onModuleUpdate(TubeModule module, PlayerEntity player);

}
