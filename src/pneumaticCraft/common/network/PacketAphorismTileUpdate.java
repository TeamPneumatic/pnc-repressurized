package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;
import cpw.mods.fml.common.network.ByteBufUtils;

public class PacketAphorismTileUpdate extends LocationIntPacket<PacketAphorismTileUpdate>{

    private String[] text;

    public PacketAphorismTileUpdate(){}

    public PacketAphorismTileUpdate(TileEntityAphorismTile tile){
        super(tile.xCoord, tile.yCoord, tile.zCoord);
        text = tile.getTextLines();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(text.length);
        for(String line : text) {
            ByteBufUtils.writeUTF8String(buffer, line);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        int lines = buffer.readInt();
        text = new String[lines];
        for(int i = 0; i < lines; i++) {
            text[i] = ByteBufUtils.readUTF8String(buffer);
        }
    }

    @Override
    public void handleClientSide(PacketAphorismTileUpdate message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketAphorismTileUpdate message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te instanceof TileEntityAphorismTile) {
            ((TileEntityAphorismTile)te).setTextLines(message.text);
        }
    }

}
