package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * Sent by server when programmer GUI is being opened
 * Sent by client programmer GUI to push the current program to the server-side TE
 */
public class PacketProgrammerUpdate extends LocationIntPacket implements ILargePayload {
    private List<IProgWidget> widgets;
    private TileEntityProgrammer te;

    public PacketProgrammerUpdate() {
    }

    public PacketProgrammerUpdate(TileEntityProgrammer te) {
        super(te.getPos());
        this.te = te;
    }

    public PacketProgrammerUpdate(PacketBuffer buffer) {
        super(buffer);
        widgets = TileEntityProgrammer.readWidgetsFromPacket(buffer);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        te.writeProgWidgetsToPacket(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> updateTE(ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    private void updateTE(PlayerEntity player) {
        PacketUtil.getTE(player, pos, TileEntityProgrammer.class).ifPresent(te -> te.setProgWidgets(widgets, player));
    }

    @Override
    public PacketBuffer dumpToBuffer() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        toBytes(buf);
        return buf;
    }

    @Override
    public void handleLargePayload(PlayerEntity player) {
        updateTE(player);
    }
}
