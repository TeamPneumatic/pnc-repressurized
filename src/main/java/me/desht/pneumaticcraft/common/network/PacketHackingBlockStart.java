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

import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sent by client when player initiates a hack, and from server back to client to confirm initiation
 */
public class PacketHackingBlockStart extends LocationIntPacket {
    public PacketHackingBlockStart(BlockPos pos) {
        super(pos);
    }

    public PacketHackingBlockStart(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                // client
                Player cPlayer = ClientUtils.getClientPlayer();
                CommonArmorHandler.getHandlerForPlayer()
                        .getExtensionData(ArmorUpgradeRegistry.getInstance().hackHandler)
                        .setHackedBlockPos(new WorldAndCoord(cPlayer.level, pos));

                RenderBlockTarget target = ArmorUpgradeClientRegistry.getInstance()
                        .getClientHandler(ArmorUpgradeRegistry.getInstance().blockTrackerHandler, BlockTrackerClientHandler.class)
                        .getTargetForCoord(pos);
                if (target != null) target.onHackConfirmServer();
            } else {
                // server
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().blockTrackerHandler, true)) {
                    handler.getExtensionData(ArmorUpgradeRegistry.getInstance().hackHandler)
                            .setHackedBlockPos(new WorldAndCoord(player.level, pos));
                    NetworkHandler.sendToAllTracking(this, player.level, player.blockPosition());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
