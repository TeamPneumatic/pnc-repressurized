/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.api.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.semiblock.ISyncableSemiblockItem;
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
    private final int entityID;  // -1 indicates no entity, sync'ing to item in hand
    private final PacketBuffer payload;

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
            if (sender.containerMenu instanceof ISyncableSemiblockItem) {
                ((ISyncableSemiblockItem) sender.containerMenu).syncSemiblockItemFromClient(sender, payload);
            }
        } else {
            processEntity(sender.getLevel());
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
            Log.warning("PacketSemiBlockSync: did not get expected ISemiBlock entity for entity ID %d", entityID);
        }
    }
}