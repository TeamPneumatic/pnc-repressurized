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
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to set the status of multiple armor features at once
 * (used in armor init and core components reset)
 */
public record PacketToggleArmorFeatureBulk(List<FeatureSetting> features) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("toggle_armor_feature_bulk");

    public static PacketToggleArmorFeatureBulk fromNetwork(FriendlyByteBuf buffer) {
        return new PacketToggleArmorFeatureBulk(buffer.readList(FeatureSetting::fromNetwork));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(features, (b, feature) -> feature.toBytes(b));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketToggleArmorFeatureBulk message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            message.features().forEach(f -> {
                if (f.featureIndex >= 0 && f.featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(f.slot).size()
                        && PneumaticArmorItem.isPneumaticArmorPiece(player, f.slot))
                {
                    handler.setUpgradeEnabled(f.slot, f.featureIndex, f.state);
                }
            });
        }));
    }

    public record FeatureSetting(EquipmentSlot slot, byte featureIndex, boolean state) {
        static FeatureSetting fromNetwork(FriendlyByteBuf buffer) {
            return new FeatureSetting(buffer.readEnum(EquipmentSlot.class), buffer.readByte(), buffer.readBoolean());
        }

        void toBytes(FriendlyByteBuf buffer) {
            buffer.writeEnum(slot);
            buffer.writeByte(featureIndex);
            buffer.writeBoolean(state);
        }
    }
}
