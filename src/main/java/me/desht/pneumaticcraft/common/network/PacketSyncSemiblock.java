package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 *
 * <p>Sent by server to sync settings needed for GUI purposes.</p>
 * <p>Sent by client to send updated settings from GUI code</p>
 */
public class PacketSyncSemiblock {
    private int entityID;  // -1 indicates no entity, sync'ing to item in hand
    private PacketBuffer payload;

    public PacketSyncSemiblock() {
    }

    public PacketSyncSemiblock(ISemiBlock semiBlock) {
        this.entityID = semiBlock.getTrackingId();
        this.payload = new PacketBuffer(Unpooled.buffer());
        semiBlock.writeToBuf(payload);
    }

    PacketSyncSemiblock(PacketBuffer buffer) {
        this.entityID = buffer.readInt();
        int size = buffer.readVarInt();
        this.payload = new PacketBuffer(Unpooled.buffer());
        buffer.readBytes(this.payload, size);
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(entityID);
        buffer.writeVarInt(payload.writerIndex());
        buffer.writeBytes(payload);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
           if (ctx.get().getSender() == null) {
               handleClient();
           } else {
               handleServer(ctx.get().getSender());
           }
        });
        ctx.get().setPacketHandled(true);
    }

    private void handleServer(ServerPlayerEntity sender) {
        if (entityID == -1) {
            // TODO make this more general.  Right now we only have a container for logistics frames but maybe not forever...
            if (sender.openContainer instanceof ContainerLogistics) {
                ((ContainerLogistics) sender.openContainer).updateHeldItem(sender, payload);
            }
        } else {
            processEntity(sender.getServerWorld());
        }
    }

    private void handleClient() {
        Validate.isTrue(entityID >= 0);
        processEntity(ClientUtils.getClientWorld());
    }

    private void processEntity(World world) {
        ISemiBlock semiBlock = ISemiBlock.byTrackingId(world, entityID);
        if (semiBlock != null) {
            semiBlock.readFromBuf(payload);
        } else {
            Log.warning("PacketSemiBlockSync: did not get expected ISemiBlock entity for entity ID %s", entityID);
        }
    }
}