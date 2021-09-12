package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker.TrackerBlacklistManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
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
    private final CompoundNBT tag;

    public PacketSendNBTPacket(TileEntity te) {
        super(te.getBlockPos());

        tag = te.save(new CompoundNBT());
    }

    public PacketSendNBTPacket(PacketBuffer buffer) {
        super(buffer);
        tag = buffer.readNbt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TileEntity te = ClientUtils.getClientTE(pos);
            if (te != null) {
                try {
                    te.load(te.getBlockState(), tag);
                } catch (Throwable e) {
                    TrackerBlacklistManager.addInventoryTEToBlacklist(te, e);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
