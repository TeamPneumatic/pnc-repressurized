package me.desht.pneumaticcraft.common.network;

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.util.UUID;

public class PacketSecurityStationAddHacker extends PacketSecurityStation {

    public PacketSecurityStationAddHacker() {
    }

    public PacketSecurityStationAddHacker(TileEntity te, UUID username) {
        super(te, username);
    }

    public PacketSecurityStationAddHacker(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected void handle(TileEntity te, UUID uuid) {
        if (te instanceof TileEntitySecurityStation) {
            ((TileEntitySecurityStation) te).addHacker(new GameProfile(uuid, null));
        }
    }
}
