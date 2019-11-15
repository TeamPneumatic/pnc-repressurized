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

    public PacketAphorismTileUpdate() {
    }

    public PacketAphorismTileUpdate(PacketBuffer buffer) {
        super(buffer);
        int lines = buffer.readInt();
        text = new String[lines];
        for (int i = 0; i < lines; i++) {
            text[i] = buffer.readString();
        }
    }

    public PacketAphorismTileUpdate(TileEntityAphorismTile tile) {
        super(tile.getPos());
        text = tile.getTextLines();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeInt(text.length);
        Arrays.stream(text).forEach(buffer::writeString);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            if (te instanceof TileEntityAphorismTile) {
                ((TileEntityAphorismTile) te).setTextLines(text);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
