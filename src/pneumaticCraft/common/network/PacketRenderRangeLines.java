package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.IRangeLineShower;

public class PacketRenderRangeLines extends LocationIntPacket<PacketRenderRangeLines>{

    public PacketRenderRangeLines(){}

    public PacketRenderRangeLines(TileEntity te){
        super(te.xCoord, te.yCoord, te.zCoord);
    }

    @Override
    public void handleClientSide(PacketRenderRangeLines message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te instanceof IRangeLineShower) {
            ((IRangeLineShower)te).showRangeLines();
        }
    }

    @Override
    public void handleServerSide(PacketRenderRangeLines message, EntityPlayer player){}

}
