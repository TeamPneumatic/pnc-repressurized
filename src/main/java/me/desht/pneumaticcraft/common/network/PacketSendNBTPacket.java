package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryInventory;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class PacketSendNBTPacket extends LocationIntPacket<PacketSendNBTPacket> {

    private NBTTagCompound tag;

    public PacketSendNBTPacket() {
    }

    public PacketSendNBTPacket(TileEntity te) {
        super(te.getPos());
        tag = new NBTTagCompound();
        te.writeToNBT(tag);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        try {
            new PacketBuffer(buffer).writeCompoundTag(tag);
        } catch (Exception e) {
            Log.error("An exception occured when trying to encode a Send NBT Packet.");
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        try {
            tag = new PacketBuffer(buffer).readCompoundTag();
        } catch (Exception e) {
            Log.error("An exception occured when trying to decode a Send NBT Packet.");
            e.printStackTrace();
        }
    }

    @Override
    public void handleClientSide(PacketSendNBTPacket message, EntityPlayer player) {
        TileEntity te = message.getTileEntity(player.getEntityWorld());
        if (te != null) {
            try {
                te.readFromNBT(message.tag);
            } catch (Throwable e) {
                BlockTrackEntryInventory.addTileEntityToBlackList(te, e);
            }
        }
    }

    @Override
    public void handleServerSide(PacketSendNBTPacket message, EntityPlayer player) {
    }

}
