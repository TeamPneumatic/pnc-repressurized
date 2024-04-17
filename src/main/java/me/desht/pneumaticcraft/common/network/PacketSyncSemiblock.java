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
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.apache.commons.lang3.Validate;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * <p>Sent by server to sync settings needed for GUI purposes.</p>
 * <p>Sent by client to send updated settings from GUI code</p>
 */
public record PacketSyncSemiblock(int entityID, FriendlyByteBuf payload) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("sync_semiblock");

    public static PacketSyncSemiblock create(ISemiBlock semiBlock, boolean itemContainer) {
        FriendlyByteBuf payload = Util.make(new FriendlyByteBuf(Unpooled.buffer()), semiBlock::writeToBuf);

        return new PacketSyncSemiblock(itemContainer ? -1 : semiBlock.getTrackingId(), payload);
    }

    public static PacketSyncSemiblock fromNetwork(FriendlyByteBuf buffer) {
        int entityID = buffer.readInt();
        int size = buffer.readVarInt();
        FriendlyByteBuf payload = new FriendlyByteBuf(Unpooled.buffer(size));
        buffer.readBytes(payload, size);

        return new PacketSyncSemiblock(entityID, payload);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(entityID);
        int size = payload.writerIndex();
        buffer.writeVarInt(size);
        buffer.writeBytes(payload, 0, size);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSyncSemiblock message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
           if (ctx.flow().isClientbound()) {
               message.handleClient();
           } else if (player instanceof ServerPlayer sp) {
               message.handleServer(sp);
           }
        }));
    }

    private void handleServer(ServerPlayer sender) {
        if (entityID == -1) {
            if (sender.containerMenu instanceof ISyncableSemiblockItem syncable) {
                syncable.syncSemiblockItemFromClient(sender, payload);
            } else {
                Log.warning("PacketSyncSemiblock: received packet with entity -1, but player is not holding a semiblock item?");
            }
        } else {
            processEntity(sender.level());
        }
    }

    private void handleClient() {
        Validate.isTrue(entityID >= 0);
        processEntity(ClientUtils.getClientLevel());
    }

    private void processEntity(Level world) {
        ISemiBlock semiBlock = ISemiBlock.byTrackingId(world, entityID);
        if (semiBlock != null) {
            semiBlock.readFromBuf(payload);
        } else {
            Log.warning("PacketSyncSemiblock: did not get expected ISemiBlock entity for entity ID {}", entityID);
        }
    }
}