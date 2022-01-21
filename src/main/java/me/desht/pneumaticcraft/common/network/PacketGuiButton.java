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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent when a GUI button is clicked.
 */
public class PacketGuiButton {
    private final String tag;
    private final boolean shiftHeld;

    public PacketGuiButton(String tag) {
        this.tag = tag;
        this.shiftHeld = ClientUtils.hasShiftDown();
    }

    public PacketGuiButton(FriendlyByteBuf buffer) {
        tag = buffer.readUtf(1024);
        shiftHeld = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(tag);
        buffer.writeBoolean(shiftHeld);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu instanceof IGUIButtonSensitive) {
                ((IGUIButtonSensitive) player.containerMenu).handleGUIButtonPress(tag, shiftHeld, player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
