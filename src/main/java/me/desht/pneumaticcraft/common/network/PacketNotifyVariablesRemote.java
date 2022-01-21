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
import me.desht.pneumaticcraft.common.inventory.ContainerRemote;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when the Remote GUI is being opened
 * TODO: should be possible to include this data in the open gui message?
 */
public class PacketNotifyVariablesRemote {
    private final String[] variables;

    public PacketNotifyVariablesRemote(String[] variables) {
        this.variables = variables;
    }

    public PacketNotifyVariablesRemote(FriendlyByteBuf buffer) {
        variables = new String[buffer.readVarInt()];
        for (int i = 0; i < variables.length; i++) {
            variables[i] = buffer.readUtf();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(variables.length);
        Arrays.stream(variables).forEach(buf::writeUtf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ClientUtils.getClientPlayer();
            if (player.containerMenu instanceof ContainerRemote) {
                ((ContainerRemote) player.containerMenu).variables = variables;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
