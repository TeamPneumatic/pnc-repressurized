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

import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sent by client when player initiates a hack, and from server back to client to confirm initiation
 */
public record PacketHackingBlockStart(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketHackingBlockStart> TYPE = new Type<>(RL("hack_block_start"));

    public static final StreamCodec<FriendlyByteBuf, PacketHackingBlockStart> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketHackingBlockStart::pos,
            PacketHackingBlockStart::new
    );

    @Override
    public Type<PacketHackingBlockStart> type() {
        return TYPE;
    }

    public static void handle(PacketHackingBlockStart message, IPayloadContext ctx) {
        Player player = ctx.player();

        if (ctx.flow().isClientbound()) {
            // client
            CommonArmorHandler.getHandlerForPlayer()
                    .getExtensionData(CommonUpgradeHandlers.hackHandler)
                    .setHackedBlockPos(GlobalPos.of(player.level().dimension(), message.pos));

            RenderBlockTarget target = ClientArmorRegistry.getInstance()
                    .getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class)
                    .getTargetForCoord(message.pos);
            if (target != null) {
                target.onHackConfirmedByServer();
            }
        } else if (player instanceof ServerPlayer sp) {
            // server
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(CommonUpgradeHandlers.blockTrackerHandler, true)) {
                handler.getExtensionData(CommonUpgradeHandlers.hackHandler)
                        .setHackedBlockPos(GlobalPos.of(player.level().dimension(), message.pos));
                NetworkHandler.sendToPlayer(message, sp);
            }
        }
    }
}
