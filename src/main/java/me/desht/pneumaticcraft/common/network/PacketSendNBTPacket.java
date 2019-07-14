package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.TrackerBlacklistManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to give a clientside TE a copy of its NBT data
 */
public class PacketSendNBTPacket extends LocationIntPacket {

    private CompoundNBT tag;

    public PacketSendNBTPacket() {
    }

    public PacketSendNBTPacket(TileEntity te) {
        super(te.getPos());
        tag = new CompoundNBT();
        te.write(tag);
    }

    public PacketSendNBTPacket(PacketBuffer buffer) {
        super(buffer);
        try {
            tag = new PacketBuffer(buffer).readCompoundTag();
        } catch (Exception e) {
            Log.error("An exception occured when trying to decode a Send NBT Packet.");
            e.printStackTrace();
        }
    }

    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        try {
            new PacketBuffer(buffer).writeCompoundTag(tag);
        } catch (Exception e) {
            Log.error("An exception occured when trying to encode a Send NBT Packet.");
            e.printStackTrace();
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = getTileEntity(ctx);
            if (te != null) {
                try {
                    te.read(tag);
                } catch (Throwable e) {
                    TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
