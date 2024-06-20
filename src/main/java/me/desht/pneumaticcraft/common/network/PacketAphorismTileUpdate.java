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

import me.desht.pneumaticcraft.common.block.AphorismTileBlock;
import me.desht.pneumaticcraft.common.block.entity.utility.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by the client after editing an Aphorism Tile
 */
public record PacketAphorismTileUpdate(BlockPos pos, List<String> text, int textRotation, byte margin, boolean invis) implements CustomPacketPayload {
    private static final int MAX_LENGTH = 1024;

    public static final Type<PacketAphorismTileUpdate> TYPE = new Type<>(RL("aphorism_tile_update"));

    public static final StreamCodec<FriendlyByteBuf, PacketAphorismTileUpdate> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketAphorismTileUpdate::pos,
            ByteBufCodecs.stringUtf8(MAX_LENGTH).apply(ByteBufCodecs.list()), PacketAphorismTileUpdate::text,
            ByteBufCodecs.VAR_INT, PacketAphorismTileUpdate::textRotation,
            ByteBufCodecs.BYTE, PacketAphorismTileUpdate::margin,
            ByteBufCodecs.BOOL, PacketAphorismTileUpdate::invis,
            PacketAphorismTileUpdate::new
    );

    public static PacketAphorismTileUpdate forBlockEntity(AphorismTileBlockEntity blockEntity) {
        return new PacketAphorismTileUpdate(blockEntity.getBlockPos(), List.of(blockEntity.getTextLines()), blockEntity.getTextRotation(),
                blockEntity.getMarginSize(), blockEntity.getBlockState().getValue(AphorismTileBlock.INVISIBLE));
    }

    @Override
    public Type<PacketAphorismTileUpdate> type() {
        return TYPE;
    }

    public static void handle(PacketAphorismTileUpdate message, IPayloadContext ctx) {
            Player player = ctx.player();
            if (PneumaticCraftUtils.canPlayerReach(player, message.pos())) {
                PneumaticCraftUtils.getBlockEntityAt(player.level(), message.pos(), AphorismTileBlockEntity.class).ifPresent(te -> {
                    te.setTextLines(message.text().toArray(new String[0]), false);
                    te.setTextRotation(message.textRotation());
                    te.setMarginSize(message.margin());
                    te.setInvisible(message.invis());
                });
            }
    }
}
