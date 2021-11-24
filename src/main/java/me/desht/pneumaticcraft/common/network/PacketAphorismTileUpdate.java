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

import me.desht.pneumaticcraft.common.block.BlockAphorismTile;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by the client after editing an Aphorism Tile
 */
public class PacketAphorismTileUpdate extends LocationIntPacket {

    private static final int MAX_LENGTH = 1024;
    private final String[] text;
    private final int textRotation;
    private final byte margin;
    private final boolean invis;

    public PacketAphorismTileUpdate(PacketBuffer buffer) {
        super(buffer);

        textRotation = buffer.readByte();
        int lines = buffer.readVarInt();
        text = new String[lines];
        for (int i = 0; i < lines; i++) {
            text[i] = buffer.readUtf(MAX_LENGTH);
        }
        margin = buffer.readByte();
        invis = buffer.readBoolean();
    }

    public PacketAphorismTileUpdate(TileEntityAphorismTile tile) {
        super(tile.getBlockPos());

        text = tile.getTextLines();
        textRotation = tile.textRotation;
        margin = tile.getMarginSize();
        invis = tile.getBlockState().getValue(BlockAphorismTile.INVISIBLE);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeByte(textRotation);
        buffer.writeVarInt(text.length);
        Arrays.stream(text).forEach(s -> buffer.writeUtf(s, MAX_LENGTH));
        buffer.writeByte(margin);
        buffer.writeBoolean(invis);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Objects.requireNonNull(ctx.get().getSender());
            if (PneumaticCraftUtils.canPlayerReach(player, pos)) {
                PneumaticCraftUtils.getTileEntityAt(player.level, pos, TileEntityAphorismTile.class).ifPresent(te -> {
                    te.setTextLines(text, false);
                    te.textRotation = textRotation;
                    te.setMarginSize(margin);
                    te.setInvisible(invis);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
