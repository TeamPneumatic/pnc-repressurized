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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent from Pneumatic Armor colors GUI to re-color armor pieces
 */
public record PacketUpdateArmorColors(int[][] cols, int eyepiece) implements CustomPacketPayload {
    public static final Type<PacketUpdateArmorColors> TYPE = new Type<>(RL("update_armor_colors"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateArmorColors> STREAM_CODEC = StreamCodec.of(
            PacketUpdateArmorColors::write,
            PacketUpdateArmorColors::fromNetwork
    );

    public static PacketUpdateArmorColors forPlayer(Player player) {
        int [][] cols = new int[4][2];
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof PneumaticArmorItem p) {
                cols[slot.getIndex()][0] = p.getPrimaryColor(stack);
                cols[slot.getIndex()][1] = p.getSecondaryColor(stack);
            }
        }
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        int eyepiece = stack.getItem() instanceof PneumaticArmorItem p ? p.getEyepieceColor(stack) : 0;

        return new PacketUpdateArmorColors(cols, eyepiece);
    }

    public static PacketUpdateArmorColors fromNetwork(FriendlyByteBuf buffer) {
        int [][] cols = new int[4][2];
        for (int i = 0; i < cols.length; i++) {
            cols[i][0] = buffer.readInt();
            cols[i][1] = buffer.readInt();
        }
        return new PacketUpdateArmorColors(cols, buffer.readInt());
    }

    public static void write(FriendlyByteBuf buffer, PacketUpdateArmorColors message) {
        for (int[] col : message.cols) {
            buffer.writeInt(col[0]);
            buffer.writeInt(col[1]);
        }
        buffer.writeInt(message.eyepiece);
    }

    @Override
    public Type<PacketUpdateArmorColors> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateArmorColors message, IPayloadContext ctx) {
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = ctx.player().getItemBySlot(slot);
            if (stack.getItem() instanceof PneumaticArmorItem p) {
                stack.set(DataComponents.DYED_COLOR, new DyedItemColor(message.cols()[slot.getIndex()][0], true));
                p.setPrimaryColor(stack, message.cols()[slot.getIndex()][1]);
                p.setSecondaryColor(stack, message.cols()[slot.getIndex()][1]);
            }
        }
        ItemStack stack = ctx.player().getItemBySlot(EquipmentSlot.HEAD);
        if (stack.getItem() instanceof PneumaticArmorItem p) {
            p.setEyepieceColor(stack, message.eyepiece());
        }
    }
}
