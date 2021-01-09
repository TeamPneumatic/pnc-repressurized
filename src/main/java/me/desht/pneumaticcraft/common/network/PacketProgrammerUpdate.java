package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * Sent by server when programmer GUI is being opened
 * Sent by client programmer GUI to push the current program to the server-side TE
 */
public class PacketProgrammerUpdate extends LocationIntPacket implements ILargePayload {
    private final List<IProgWidget> widgets;

    public PacketProgrammerUpdate(TileEntityProgrammer te) {
        super(te.getPos());
        this.widgets = te.progWidgets;
    }

    public PacketProgrammerUpdate(PacketBuffer buffer) {
        super(buffer);
        widgets = readWidgetsFromPacket(buffer);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        writeProgWidgetsToPacket(buffer);
    }

    private void writeProgWidgetsToPacket(PacketBuffer buf) {
        buf.writeVarInt(widgets.size());
        for (IProgWidget progWidget : widgets) {
            progWidget.writeToPacket(buf);
        }
    }

    private static List<IProgWidget> readWidgetsFromPacket(PacketBuffer buf) {
        List<IProgWidget> widgets = new ArrayList<>();
        int nWidgets = buf.readVarInt();
        for (int i = 0; i < nWidgets; i++) {
            try {
                IProgWidget widget = ProgWidget.fromPacket(buf);
                if (!widget.isAvailable()) {
                    Log.warning("ignoring unavailable widget type " + widget.getTypeID().toString());
                } else {
                    widgets.add(widget);
                }
            } catch (IllegalStateException e) {
                Log.warning(e.getMessage());
            }
        }
        return widgets;
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
