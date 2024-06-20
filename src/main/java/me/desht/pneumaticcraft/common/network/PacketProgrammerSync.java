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

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.common.block.entity.drone.ProgrammerBlockEntity;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;
import me.desht.pneumaticcraft.common.inventory.ProgrammerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * Sent by server when programmer GUI is being opened
 * Sent by client programmer GUI to push the current program to the server-side BE
 */
public record PacketProgrammerSync(BlockPos pos, List<IProgWidget> widgets) implements CustomPacketPayload {
    public static final Type<PacketProgrammerSync> TYPE = new Type<>(RL("programmer_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketProgrammerSync> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketProgrammerSync::pos,
            ProgWidget.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketProgrammerSync::widgets,
            PacketProgrammerSync::new
    );

    public static PacketProgrammerSync forBlockEntity(ProgrammerBlockEntity te) {
        return new PacketProgrammerSync(te.getBlockPos(), te.progWidgets);
    }

    @Override
    public Type<PacketProgrammerSync> type() {
        return TYPE;
    }

    public static void handle(PacketProgrammerSync message, IPayloadContext ctx) {
        if (ctx.player().isLocalPlayer() || ctx.player().containerMenu instanceof ProgrammerMenu) {
            PacketUtil.getBlockEntity(ctx.player(), message.pos, ProgrammerBlockEntity.class)
                    .ifPresent(te -> te.setProgWidgets(message.widgets, ctx.player()));
        }
    }
}
