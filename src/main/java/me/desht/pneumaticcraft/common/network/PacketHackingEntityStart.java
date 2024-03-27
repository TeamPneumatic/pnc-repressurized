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
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderEntityTarget;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sent by client when player initiates an entity hack, and by server to confirm initiation
 */
public record PacketHackingEntityStart(int entityId) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("hack_entity_start");

    public static PacketHackingEntityStart fromNetwork(FriendlyByteBuf buffer) {
        return new PacketHackingEntityStart(buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketHackingEntityStart message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (ctx.flow().isClientbound()) {
                // client
                Entity entity = player.level().getEntity(message.entityId());
                if (entity != null) {
                    CommonArmorHandler.getHandlerForPlayer(player)
                            .getExtensionData(CommonUpgradeHandlers.hackHandler)
                            .setHackedEntity(entity);
                    ClientArmorRegistry.getInstance()
                            .getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class)
                            .getTargetsStream()
                            .filter(target -> target.entity == entity)
                            .findFirst()
                            .ifPresent(RenderEntityTarget::onHackConfirmServer);
                }
            } else if (player instanceof ServerPlayer sp) {
                // server
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.upgradeUsable(CommonUpgradeHandlers.entityTrackerHandler, true)) {
                    Entity entity = player.level().getEntity(message.entityId());
                    if (entity != null) {
                        handler.getExtensionData(CommonUpgradeHandlers.hackHandler).setHackedEntity(entity);
                        NetworkHandler.sendToPlayer(message, sp);
                    }
                }
            }
        }));
    }
}
