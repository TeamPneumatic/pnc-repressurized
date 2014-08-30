package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public class PacketDescriptionPacketRequest extends LocationIntPacket<PacketDescriptionPacketRequest>{

    public PacketDescriptionPacketRequest(){}

    public PacketDescriptionPacketRequest(int x, int y, int z){
        super(x, y, z);
    }

    @Override
    public void handleClientSide(PacketDescriptionPacketRequest message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketDescriptionPacketRequest message, EntityPlayer player){
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(te != null) {
            NetworkHandler.sendTo(new PacketSendNBTPacket(te), (EntityPlayerMP)player);
        }
    }

}