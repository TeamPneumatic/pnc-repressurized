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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sent by client to switch an armor module on or off
 * Sent by server to initiate the process on the client (client will send this packet back in response if the module
 *   was actually changed)
 */
public record PacketToggleArmorFeature(EquipmentSlot slot, byte featureIndex, boolean state) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("toggle_armor_feature");

    public static PacketToggleArmorFeature fromNetwork(FriendlyByteBuf buffer) {
        return new PacketToggleArmorFeature(buffer.readEnum(EquipmentSlot.class), buffer.readByte(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(slot);
        buf.writeByte(featureIndex);
        buf.writeBoolean(state);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketToggleArmorFeature message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            EquipmentSlot slot = message.slot();
            byte featureIndex = message.featureIndex();
            boolean state = message.state();

            if (ctx.flow().isClientbound()) {
                ClientUtils.setArmorUpgradeEnabled(slot, featureIndex, state);
            } else {
                if (featureIndex >= 0
                        && featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).size()
                        && (PneumaticArmorItem.isPneumaticArmorPiece(player, slot) || slot == EquipmentSlot.HEAD && featureIndex == 0)) {
                    CommonArmorHandler.getHandlerForPlayer(player).setUpgradeEnabled(slot, featureIndex, state);
                }
            }
        }));
    }
}
