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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when an entity hack completes
 */
public record PacketHackingEntityFinish(int entityId) implements CustomPacketPayload {
    public static final Type<PacketHackingEntityFinish> TYPE = new Type<>(RL("hack_entity_finish"));

    public static final StreamCodec<FriendlyByteBuf, PacketHackingEntityFinish> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketHackingEntityFinish::entityId,
            PacketHackingEntityFinish::new
    );

    public static PacketHackingEntityFinish forEntity(Entity entity) {
        return new PacketHackingEntityFinish(entity.getId());
    }

    @Override
    public Type<PacketHackingEntityFinish> type() {
        return TYPE;
    }

    public static void handle(PacketHackingEntityFinish message, IPayloadContext ctx) {
        Player player = ctx.player();

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
    }
}
