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

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to set the status of multiple armor features at once
 * (used in armor init & core components reset)
 */
public class PacketToggleArmorFeatureBulk {
    private final List<FeatureSetting> features;

    public PacketToggleArmorFeatureBulk(List<FeatureSetting> features) {
        this.features = features;
    }

    PacketToggleArmorFeatureBulk(PacketBuffer buffer) {
        this.features = new ArrayList<>();
        int len = buffer.readVarInt();
        for (int i = 0; i < len; i++) {
            features.add(new FeatureSetting(buffer));
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(features.size());
        features.forEach(f -> f.toBytes(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player != null) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                features.forEach(f -> {
                    if (f.featureIndex >= 0 && f.featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(f.slot).size()
                        && ItemPneumaticArmor.isPneumaticArmorPiece(player, f.slot))
                    {
                        handler.setUpgradeEnabled(f.slot, f.featureIndex, f.state);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static class FeatureSetting {
        private final EquipmentSlotType slot;
        private final byte featureIndex;
        private final boolean state;

        FeatureSetting(PacketBuffer buffer) {
            this(EquipmentSlotType.values()[buffer.readByte()], buffer.readByte(), buffer.readBoolean());
        }

        public FeatureSetting(EquipmentSlotType slot, byte featureIndex, boolean state) {
            this.slot = slot;
            this.featureIndex = featureIndex;
            this.state = state;
        }

        void toBytes(PacketBuffer buffer) {
            buffer.writeByte(slot.ordinal());
            buffer.writeByte(featureIndex);
            buffer.writeBoolean(state);
        }
    }
}
