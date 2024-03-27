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
import me.desht.pneumaticcraft.common.block.entity.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.drone.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.WidgetSerializer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * Sent by server when programmer GUI is being opened
 * Sent by client programmer GUI to push the current program to the server-side BE
 */
public record PacketProgrammerSync(BlockPos pos, List<IProgWidget> widgets) implements CustomPacketPayload, ILargePayload {
    public static final ResourceLocation ID = RL("programmer_sync");

    public static PacketProgrammerSync forBlockEntity(ProgrammerBlockEntity te) {
        return new PacketProgrammerSync(te.getBlockPos(), te.progWidgets);
    }

    public static PacketProgrammerSync fromNetwork(FriendlyByteBuf buffer) {
        return new PacketProgrammerSync(buffer.readBlockPos(), WidgetSerializer.readWidgetsFromPacket(buffer));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        WidgetSerializer.writeProgWidgetsToPacket(widgets, buffer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketProgrammerSync message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> message.updateTE(player)));
    }

    private void updateTE(Player player) {
        PacketUtil.getBlockEntity(player, pos, ProgrammerBlockEntity.class)
                .ifPresent(te -> te.setProgWidgets(widgets, player));
    }

    @Override
    public FriendlyByteBuf dumpToBuffer() {
        return Util.make(new FriendlyByteBuf(Unpooled.buffer()), this::write);
    }

    @Override
    public void handleLargePayload(Player player) {
        updateTE(player);
    }

}
