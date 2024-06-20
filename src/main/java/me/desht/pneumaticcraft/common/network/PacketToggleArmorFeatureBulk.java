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
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature.FeatureSetting;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to set the status of multiple armor features at once
 * (used in armor init and core components reset)
 */
public record PacketToggleArmorFeatureBulk(List<FeatureSetting> features) implements CustomPacketPayload {
    public static final Type<PacketToggleArmorFeatureBulk> TYPE = new Type<>(RL("toggle_armor_feature_bulk"));

    public static final StreamCodec<FriendlyByteBuf, PacketToggleArmorFeatureBulk> STREAM_CODEC = StreamCodec.composite(
            FeatureSetting.STREAM_CODEC.apply(ByteBufCodecs.list()), PacketToggleArmorFeatureBulk::features,
            PacketToggleArmorFeatureBulk::new
    );

    @Override
    public Type<PacketToggleArmorFeatureBulk> type() {
        return TYPE;
    }

    public static void handle(PacketToggleArmorFeatureBulk message, IPayloadContext ctx) {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(ctx.player());
        message.features().forEach(feature -> {
            if (feature.featureIndex() >= 0 && feature.featureIndex() < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(feature.slot()).size()
                    && PneumaticArmorItem.isPneumaticArmorPiece(ctx.player(), feature.slot()))
            {
                handler.setUpgradeEnabled(feature.slot(), feature.featureIndex(), feature.state());
            }
        });
    }
}
