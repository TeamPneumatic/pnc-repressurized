package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.DamageSourcePneumaticCraft;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketSecurityStationFailedHack extends LocationIntPacket<PacketSecurityStationFailedHack> {

    public PacketSecurityStationFailedHack() {
    }

    public PacketSecurityStationFailedHack(BlockPos pos) {
        super(pos);
    }

    @Override
    public void handleClientSide(PacketSecurityStationFailedHack message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketSecurityStationFailedHack message, EntityPlayer player) {
        TileEntity te = message.getTileEntity(player.world);
        if (te instanceof TileEntitySecurityStation) {
            TileEntitySecurityStation station = (TileEntitySecurityStation) te;
            if (!station.isPlayerOnWhiteList(player)) {
                player.attackEntityFrom(DamageSourcePneumaticCraft.SECURITY_STATION, 19);
            }
        }
    }
}
