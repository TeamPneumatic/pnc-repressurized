package pneumaticCraft.common.thirdparty.fmp;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.network.AbstractPacket;

public class PacketFMPPlacePart extends AbstractPacket<PacketFMPPlacePart>{

    @Override
    public void fromBytes(ByteBuf buf){

    }

    @Override
    public void toBytes(ByteBuf buf){

    }

    @Override
    public void handleClientSide(PacketFMPPlacePart message, EntityPlayer player){

    }

    @Override
    public void handleServerSide(PacketFMPPlacePart message, EntityPlayer player){
        FMPPlacementListener.place(player, player.worldObj);
    }

}
