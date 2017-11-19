package me.desht.pneumaticcraft.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
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
            forceLootGeneration(te);
            NetworkHandler.sendTo(new PacketSendNBTPacket(te), (EntityPlayerMP) player);
        }
    }
    
    /**
     * Force loot generation, as this is required on the client side to peek inside inventories.
     * The client is not able to generate the loot.
     * @param te
     */
    private void forceLootGeneration(TileEntity te){
        if(te instanceof TileEntityLockableLoot){
            TileEntityLockableLoot teLoot = (TileEntityLockableLoot)te;
            teLoot.fillWithLoot(null);
        }
    }
}