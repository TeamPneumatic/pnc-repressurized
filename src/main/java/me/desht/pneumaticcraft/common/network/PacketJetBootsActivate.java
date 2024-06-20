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

import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from client to tell the server the player is activating/deactivating jet boots.  Toggled when
 * Jump key is pressed or released.
 */
public record PacketJetBootsActivate(boolean state) implements CustomPacketPayload {
    public static final Type<PacketJetBootsActivate> TYPE = new Type<>(RL("jetboots_activate"));

    public static final StreamCodec<FriendlyByteBuf, PacketJetBootsActivate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PacketJetBootsActivate::state,
            PacketJetBootsActivate::new
    );

    @Override
    public Type<PacketJetBootsActivate> type() {
        return TYPE;
    }

    public static void handle(PacketJetBootsActivate message, IPayloadContext ctx) {
        Player player = ctx.player();

        if (PneumaticArmorItem.isPneumaticArmorPiece(player, EquipmentSlot.FEET)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            JetBootsStateTracker.JetBootsState jbState = JetBootsStateTracker.getTracker(player).getJetBootsState(player);
            if (handler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()) > 0
                    && (!message.state() || jbState.isEnabled())) {
                CommonUpgradeHandlers.jetBootsHandler.setJetBootsActive(handler, message.state());
            }
        }
    }
}
