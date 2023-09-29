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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sent by client to switch an armor module on or off
 * Sent by server to initiate the process on the client (client will send this packet back in response if the module
 *   was actually changed)
 */
public class PacketToggleArmorFeature {
    private final byte featureIndex;
    private final boolean state;
    private final EquipmentSlot slot;

    public PacketToggleArmorFeature(EquipmentSlot slot, byte featureIndex, boolean state) {
        this.featureIndex = featureIndex;
        this.state = state;
        this.slot = slot;
    }

    PacketToggleArmorFeature(FriendlyByteBuf buffer) {
        featureIndex = buffer.readByte();
        state = buffer.readBoolean();
        slot = EquipmentSlot.values()[buffer.readByte()];
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(featureIndex);
        buf.writeBoolean(state);
        buf.writeByte(slot.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getSender() != null) {
            // received on server
            ctx.get().enqueueWork(() -> {
                Player player = ctx.get().getSender();
                if (player != null && featureIndex >= 0
                        && featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).size()
                        && (PneumaticArmorItem.isPneumaticArmorPiece(player, slot) || slot == EquipmentSlot.HEAD && featureIndex == 0)) {
                    CommonArmorHandler.getHandlerForPlayer(player).setUpgradeEnabled(slot, featureIndex, state);
                }
            });
        } else {
            // received on client
            ctx.get().enqueueWork(() -> ClientUtils.setArmorUpgradeEnabled(slot, featureIndex, state));
        }
        ctx.get().setPacketHandled(true);
    }
}
