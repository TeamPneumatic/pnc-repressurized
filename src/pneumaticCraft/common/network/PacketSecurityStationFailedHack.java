package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.DamageSourcePneumaticCraft;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

public class PacketSecurityStationFailedHack extends LocationIntPacket<PacketSecurityStationFailedHack>{

    public PacketSecurityStationFailedHack(){}

    public PacketSecurityStationFailedHack(int x, int y, int z){
        super(x, y, z);
    }

    @Override
    public void handleClientSide(PacketSecurityStationFailedHack message, EntityPlayer player){}

    @Override
    public void handleServerSide(PacketSecurityStationFailedHack message, EntityPlayer player){
        TileEntity te = message.getTileEntity(player.worldObj);
        if(te instanceof TileEntitySecurityStation) {
            TileEntitySecurityStation station = (TileEntitySecurityStation)te;
            if(!station.isPlayerOnWhiteList(player)) {
                player.attackEntityFrom(DamageSourcePneumaticCraft.securityStation, 19);
            }
        }
    }
}
