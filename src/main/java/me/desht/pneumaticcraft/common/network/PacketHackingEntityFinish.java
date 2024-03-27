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

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.HackTickTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when an entity hack completes
 */
public record PacketHackingEntityFinish(int entityId) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("hack_entity_finish");

    public static PacketHackingEntityFinish forEntity(Entity entity) {
        return new PacketHackingEntityFinish(entity.getId());
    }

    public static PacketHackingEntityFinish fromNetwork(FriendlyByteBuf buffer) {
        return new PacketHackingEntityFinish(buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketHackingEntityFinish message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            Entity entity = player.level().getEntity(message.entityId());
            if (entity != null) {
                IHackableEntity<?> hackableEntity = HackManager.getHackableForEntity(entity, player);
                if (hackableEntity != null) {
                    hackableEntity._onHackFinished(entity, player);
                    HackTickTracker.getInstance(entity.level()).trackEntity(entity, hackableEntity);
                    CommonArmorHandler.getHandlerForPlayer(player).getExtensionData(CommonUpgradeHandlers.hackHandler).setHackedEntity(null);
                    player.playSound(ModSounds.HELMET_HACK_FINISH.get(), 1.0F, 1.0F);
                }
            }
        }));
    }
}
