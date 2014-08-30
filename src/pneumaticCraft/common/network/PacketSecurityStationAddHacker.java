package pneumaticCraft.common.network;

import net.minecraft.tileentity.TileEntity;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;

import com.mojang.authlib.GameProfile;

public class PacketSecurityStationAddHacker extends PacketSecurityStation<PacketSecurityStationAddHacker>{

    public PacketSecurityStationAddHacker(){}

    public PacketSecurityStationAddHacker(TileEntity te, String username){
        super(te, username);
    }

    @Override
    protected void handleServerSide(TileEntity te, String profile){
        if(te instanceof TileEntitySecurityStation) {
            ((TileEntitySecurityStation)te).addHacker(new GameProfile(null, profile));
        }
    }
}
