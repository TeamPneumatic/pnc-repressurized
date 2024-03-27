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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to ask the server for more info about a block, for Pneumatic Helmet purposes
 * TODO: replace with a more formal data request protocol
 */
public record PacketDescriptionPacketRequest(BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("description_request");

    public PacketDescriptionPacketRequest(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketDescriptionPacketRequest message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> {
            if (player instanceof ServerPlayer sp && !player.isSpectator()) {
                ctx.workHandler().submitAsync(() -> {
                    CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                    if (handler.upgradeUsable(CommonUpgradeHandlers.blockTrackerHandler, true)) {
                        BlockEntity blockEntity = player.level().getBlockEntity(message.pos);
                        if (blockEntity != null) {
                            BlockTrackLootable.INSTANCE.apply(player, blockEntity);
                            NetworkHandler.sendToPlayer(PacketSendNBTPacket.forBlockEntity(blockEntity), sp);
                        }
                    }
                });
            }
        });
    }
}