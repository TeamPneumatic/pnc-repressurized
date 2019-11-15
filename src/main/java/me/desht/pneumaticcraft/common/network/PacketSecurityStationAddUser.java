package me.desht.pneumaticcraft.common.network;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class PacketSecurityStationAddUser extends PacketSecurityStation {

    public PacketSecurityStationAddUser() {

    }

    public PacketSecurityStationAddUser(TileEntity te, String username) {
        super(te, username);
    }

    public PacketSecurityStationAddUser(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected void handle(TileEntity te, String username) {
        if (te instanceof TileEntitySecurityStation) {
            ((TileEntitySecurityStation) te).addSharedUser(new GameProfile(null, username));
        }
    }

}
