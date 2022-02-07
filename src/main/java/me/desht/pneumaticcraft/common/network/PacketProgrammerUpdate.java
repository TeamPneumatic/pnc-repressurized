/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

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
        super(te.getBlockPos());
        this.widgets = te.progWidgets;
    }

    public PacketProgrammerUpdate(FriendlyByteBuf buffer) {
        super(buffer);
        widgets = readWidgetsFromPacket(buffer);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        writeProgWidgetsToPacket(buffer);
    }

    private void writeProgWidgetsToPacket(FriendlyByteBuf buf) {
        buf.writeVarInt(widgets.size());
        for (IProgWidget progWidget : widgets) {
            progWidget.writeToPacket(buf);
        }
    }

    private static List<IProgWidget> readWidgetsFromPacket(FriendlyByteBuf buf) {
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

    private void updateTE(Player player) {
        PacketUtil.getTE(player, pos, TileEntityProgrammer.class).ifPresent(te -> te.setProgWidgets(widgets, player));
    }

    @Override
    public FriendlyByteBuf dumpToBuffer() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        toBytes(buf);
        return buf;
    }

    @Override
    public void handleLargePayload(Player player) {
        updateTE(player);
    }
}
