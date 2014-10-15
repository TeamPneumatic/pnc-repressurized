package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.DamageSourcePneumaticCraft;

public class PacketSecurityStationFailedHack extends AbstractPacket<PacketSecurityStationFailedHack>{

    @Override
    public void fromBytes(ByteBuf buf){}

    @Override
    public void toBytes(ByteBuf buf){}

    @Override
    public void handleClientSide(PacketSecurityStationFailedHack message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketSecurityStationFailedHack message, EntityPlayer player){
        player.attackEntityFrom(DamageSourcePneumaticCraft.securityStation, 19);
    }
}
