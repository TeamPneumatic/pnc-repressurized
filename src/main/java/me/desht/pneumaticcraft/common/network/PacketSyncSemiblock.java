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
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.Validate;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 *
 * <p>Sent by server to sync settings needed for GUI purposes.</p>
 * <p>Sent by client to send updated settings from GUI code</p>
 */
public record PacketSyncSemiblock(int entityID, RegistryFriendlyByteBuf payload) implements CustomPacketPayload {
    public static final Type<PacketSyncSemiblock> TYPE = new Type<>(RL("sync_semiblock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncSemiblock> STREAM_CODEC = StreamCodec.of(
            PacketSyncSemiblock::write,
            PacketSyncSemiblock::fromNetwork
    );

    public static PacketSyncSemiblock create(ISemiBlock semiBlock, boolean itemContainer, RegistryAccess registryAccess) {
        RegistryFriendlyByteBuf payload = Util.make(new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess, ConnectionType.NEOFORGE), semiBlock::writeToBuf);

        return new PacketSyncSemiblock(itemContainer ? -1 : semiBlock.getTrackingId(), payload);
    }

    private static void write(RegistryFriendlyByteBuf buffer, PacketSyncSemiblock message) {
        buffer.writeInt(message.entityID);
        int size = message.payload.writerIndex();
        buffer.writeVarInt(size);
        buffer.writeBytes(message.payload, 0, size);
    }

    private static PacketSyncSemiblock fromNetwork(RegistryFriendlyByteBuf buffer) {
        int entityID = buffer.readInt();
        int size = buffer.readVarInt();
        RegistryFriendlyByteBuf payload = new RegistryFriendlyByteBuf(Unpooled.buffer(size), buffer.registryAccess(), ConnectionType.NEOFORGE);
        buffer.readBytes(payload, size);

        return new PacketSyncSemiblock(entityID, payload);
    }

    @Override
    public Type<PacketSyncSemiblock> type() {
        return TYPE;
    }

    public static void handle(PacketSyncSemiblock message, IPayloadContext ctx) {
        if (ctx.flow().isClientbound()) {
            message.handleClient();
        } else if (ctx.player() instanceof ServerPlayer sp) {
            message.handleServer(sp);
        }
        message.payload.release();
    }

    private void handleServer(ServerPlayer sender) {
        if (entityID == -1) {
            if (sender.containerMenu instanceof ISyncableSemiblockItem syncable) {
                syncable.syncSemiblockItemFromClient(sender, payload);
            } else {
                Log.warning("PacketSyncSemiblock: received packet with entity id -1, but player is not holding a semiblock item?");
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