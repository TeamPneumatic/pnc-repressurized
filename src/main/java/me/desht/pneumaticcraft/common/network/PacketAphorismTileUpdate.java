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
import me.desht.pneumaticcraft.common.block.entity.AphorismTileBlockEntity;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Arrays;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by the client after editing an Aphorism Tile
 */
public record PacketAphorismTileUpdate(BlockPos pos, String[] text, int textRotation, byte margin, boolean invis) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("aphorism_tile_update");
    private static final int MAX_LENGTH = 1024;

    public static PacketAphorismTileUpdate fromNetwork(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        int textRotation = buffer.readByte();
        int lines = buffer.readVarInt();
        String[] text = new String[lines];
        for (int i = 0; i < lines; i++) {
            text[i] = buffer.readUtf(MAX_LENGTH);
        }
        byte margin = buffer.readByte();
        boolean invis = buffer.readBoolean();

        return new PacketAphorismTileUpdate(pos, text, textRotation, margin, invis);
    }

    public static PacketAphorismTileUpdate forBlockEntity(AphorismTileBlockEntity blockEntity) {
        return new PacketAphorismTileUpdate(blockEntity.getBlockPos(), blockEntity.getTextLines(), blockEntity.getTextRotation(),
                blockEntity.getMarginSize(), blockEntity.getBlockState().getValue(AphorismTileBlock.INVISIBLE));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByte(textRotation);
        buffer.writeVarInt(text.length);
        Arrays.stream(text).forEach(s -> buffer.writeUtf(s, MAX_LENGTH));
        buffer.writeByte(margin);
        buffer.writeBoolean(invis);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketAphorismTileUpdate message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Player player = ctx.player().orElseThrow();
            if (PneumaticCraftUtils.canPlayerReach(player, message.pos())) {
                PneumaticCraftUtils.getTileEntityAt(player.level(), message.pos(), AphorismTileBlockEntity.class).ifPresent(te -> {
                    te.setTextLines(message.text(), false);
                    te.setTextRotation(message.textRotation());
                    te.setMarginSize(message.margin());
                    te.setInvisible(message.invis());
                });
            }
        });
    }
}
