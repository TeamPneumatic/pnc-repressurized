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

import me.desht.pneumaticcraft.common.pneumatic_armor.BlockTrackLootable;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to ask the server for more info about a block, for Pneumatic Helmet purposes
 * TODO: replace with a more formal data request protocol
 */
public record PacketDescriptionPacketRequest(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketDescriptionPacketRequest> TYPE = new Type<>(RL("description_request"));

    public static final StreamCodec<FriendlyByteBuf, PacketDescriptionPacketRequest> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketDescriptionPacketRequest::pos,
            PacketDescriptionPacketRequest::new
    );

    @Override
    public Type<PacketDescriptionPacketRequest> type() {
        return TYPE;
    }

    public static void handle(PacketDescriptionPacketRequest message, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(serverPlayer);
            if (handler.upgradeUsable(CommonUpgradeHandlers.blockTrackerHandler, true)) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(message.pos);
                if (blockEntity != null) {
                    BlockTrackLootable.INSTANCE.apply(serverPlayer, blockEntity);
                    NetworkHandler.sendToPlayer(PacketSendNBTPacket.forBlockEntity(blockEntity), serverPlayer);
                }
            }
        }
    }
}