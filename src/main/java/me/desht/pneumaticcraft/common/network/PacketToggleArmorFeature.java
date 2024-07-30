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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sent by client to switch an armor module on or off
 * Sent by server to initiate the process on the client (client will send this packet back in response if the module
 *   was actually changed)
 */
public record PacketToggleArmorFeature(FeatureSetting setting) implements CustomPacketPayload {
    public static final Type<PacketToggleArmorFeature> TYPE = new Type<>(RL("toggle_armor_feature"));

    public static final StreamCodec<FriendlyByteBuf, PacketToggleArmorFeature> STREAM_CODEC = StreamCodec.composite(
            FeatureSetting.STREAM_CODEC, PacketToggleArmorFeature::setting,
            PacketToggleArmorFeature::new
    );

    @Override
    public Type<PacketToggleArmorFeature> type() {
        return TYPE;
    }

    public static void handle(PacketToggleArmorFeature message, IPayloadContext ctx) {
        EquipmentSlot slot = message.setting().slot();
        byte featureIndex = message.setting.featureIndex();
        boolean state = message.setting.state();

        if (ctx.flow().isClientbound()) {
            ClientUtils.setArmorUpgradeEnabled(slot, featureIndex, state);
        } else {
            if (featureIndex >= 0
                    && featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).size()
                    && (PneumaticArmorItem.isPneumaticArmorPiece(ctx.player(), slot) || slot == EquipmentSlot.HEAD && featureIndex == 0)) {
                CommonArmorHandler.getHandlerForPlayer(ctx.player()).setUpgradeEnabled(slot, featureIndex, state);
            }
        }
    }

    public record FeatureSetting(EquipmentSlot slot, byte featureIndex, boolean state) {
        public static final StreamCodec<FriendlyByteBuf, FeatureSetting> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(EquipmentSlot.class), FeatureSetting::slot,
                ByteBufCodecs.BYTE, FeatureSetting::featureIndex,
                ByteBufCodecs.BOOL, FeatureSetting::state,
                FeatureSetting::new
        );
    }
}
