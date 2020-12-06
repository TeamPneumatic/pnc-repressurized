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
    private String[] text;
    private int textRotation;
    private byte margin;
    private boolean invis;

    public PacketAphorismTileUpdate() {
    }

    public PacketAphorismTileUpdate(PacketBuffer buffer) {
        super(buffer);

        textRotation = buffer.readByte();
        int lines = buffer.readVarInt();
        text = new String[lines];
        for (int i = 0; i < lines; i++) {
            text[i] = buffer.readString(MAX_LENGTH);
        }
        margin = buffer.readByte();
        invis = buffer.readBoolean();
    }

    public PacketAphorismTileUpdate(TileEntityAphorismTile tile) {
        super(tile.getPos());

        text = tile.getTextLines();
        textRotation = tile.textRotation;
        margin = tile.getMarginSize();
        invis = tile.getBlockState().get(BlockAphorismTile.INVISIBLE);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeByte(textRotation);
        buffer.writeVarInt(text.length);
        Arrays.stream(text).forEach(s -> buffer.writeString(s, MAX_LENGTH));
        buffer.writeByte(margin);
        buffer.writeBoolean(invis);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Objects.requireNonNull(ctx.get().getSender());
            if (PneumaticCraftUtils.canPlayerReach(player, pos)) {
                PneumaticCraftUtils.getTileEntityAt(player.world, pos, TileEntityAphorismTile.class).ifPresent(te -> {
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
