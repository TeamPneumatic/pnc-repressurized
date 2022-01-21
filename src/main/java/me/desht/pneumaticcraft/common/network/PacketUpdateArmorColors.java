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
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent from Pneumatic Armor colors GUI to re-color armor pieces
 */
public class PacketUpdateArmorColors {
    private final int[][] cols = new int[4][2];
    private final int eyepiece;

    public PacketUpdateArmorColors() {
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = ClientUtils.getClientPlayer().getItemBySlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                cols[slot.getIndex()][0] = ((ItemPneumaticArmor) stack.getItem()).getColor(stack);
                cols[slot.getIndex()][1] = ((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack);
            }
        }
        ItemStack stack = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlot.HEAD);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            eyepiece = ((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack);
        } else {
            eyepiece = 0;
        }
    }

    public PacketUpdateArmorColors(FriendlyByteBuf buffer) {
        for (int i = 0; i < 4; i++) {
            cols[i][0] = buffer.readInt();
            cols[i][1] = buffer.readInt();
        }
        eyepiece = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        for (int i = 0; i < 4; i++) {
            buffer.writeInt(cols[i][0]);
            buffer.writeInt(cols[i][1]);
        }
        buffer.writeInt(eyepiece);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (stack.getItem() instanceof ItemPneumaticArmor) {
                        ((ItemPneumaticArmor) stack.getItem()).setColor(stack, cols[slot.getIndex()][0]);
                        ((ItemPneumaticArmor) stack.getItem()).setSecondaryColor(stack, cols[slot.getIndex()][1]);
                    }
                }
                ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
                if (stack.getItem() instanceof ItemPneumaticArmor) {
                    ((ItemPneumaticArmor) stack.getItem()).setEyepieceColor(stack, eyepiece);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
