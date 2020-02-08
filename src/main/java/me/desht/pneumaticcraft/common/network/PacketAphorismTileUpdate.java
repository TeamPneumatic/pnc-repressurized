package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Recevied on: SERVER
 * Sent by the client after editing an Aphorism Tile
 */
public class PacketAphorismTileUpdate extends LocationIntPacket {

    private String[] text;
    private int textRotation;

    public PacketAphorismTileUpdate() {
    }

    public PacketAphorismTileUpdate(PacketBuffer buffer) {
        super(buffer);
        textRotation = buffer.readByte();
        int lines = buffer.readVarInt();
        text = new String[lines];
        for (int i = 0; i < lines; i++) {
            text[i] = buffer.readString();
        }
    }

    public PacketAphorismTileUpdate(TileEntityAphorismTile tile) {
        super(tile.getPos());
        text = tile.getTextLines();
        textRotation = tile.textRotation;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeByte(textRotation);
        buffer.writeVarInt(text.length);
        Arrays.stream(text).forEach(buffer::writeString);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ctx.get().getSender().world.getTileEntity(pos);
            if (te instanceof TileEntityAphorismTile) {
                // notification not required: the client told us about the text in the first place
                ((TileEntityAphorismTile) te).setTextLines(text, false);
                ((TileEntityAphorismTile) te).textRotation = textRotation;
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
