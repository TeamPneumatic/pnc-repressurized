package me.desht.pneumaticcraft.common.network;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class PacketSecurityStationAddHacker extends PacketSecurityStation {

    public PacketSecurityStationAddHacker() {
    }

    public PacketSecurityStationAddHacker(TileEntity te, String username) {
        super(te, username);
    }

    public PacketSecurityStationAddHacker(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected void handle(TileEntity te, String username) {
        if (te instanceof TileEntitySecurityStation) {
            ((TileEntitySecurityStation) te).addHacker(new GameProfile(null, username));
        }
    }
}
