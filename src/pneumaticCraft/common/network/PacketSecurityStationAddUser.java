package pneumaticCraft.common.network;

import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

import com.mojang.authlib.GameProfile;

public class PacketSecurityStationAddUser extends PacketSecurityStation<PacketSecurityStationAddUser>{

    public PacketSecurityStationAddUser(){

    }

    public PacketSecurityStationAddUser(TileEntity te, String username){
        super(te, username);
    }

    @Override
    protected void handleServerSide(TileEntity te, String profile){
        if(te instanceof TileEntitySecurityStation) {
            ((TileEntitySecurityStation)te).addSharedUser(new GameProfile(null, profile));
        }
    }

}
