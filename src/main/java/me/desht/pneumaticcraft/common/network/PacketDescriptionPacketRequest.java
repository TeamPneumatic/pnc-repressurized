package me.desht.pneumaticcraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketDescriptionPacketRequest extends LocationIntPacket<PacketDescriptionPacketRequest> {

    public PacketDescriptionPacketRequest() {
    }

    public PacketDescriptionPacketRequest(BlockPos pos) {
        super(pos);
    }

    @Override
    public void handleClientSide(PacketDescriptionPacketRequest message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketDescriptionPacketRequest message, EntityPlayer player) {
        TileEntity te = message.getTileEntity(player.world);
        if (te != null) {
            NetworkHandler.sendTo(new PacketSendNBTPacket(te), (EntityPlayerMP) player);
        }
    }

}