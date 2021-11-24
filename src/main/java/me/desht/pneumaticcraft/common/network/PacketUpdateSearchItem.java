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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the searched item (Pneumatic Helmet search upgrade)
 */
public class PacketUpdateSearchItem {
    private final ResourceLocation itemId;

    public PacketUpdateSearchItem(Item item) {
        itemId = item.getRegistryName();
    }

    public PacketUpdateSearchItem(PacketBuffer buffer) {
        itemId = buffer.readResourceLocation();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeResourceLocation(itemId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().searchHandler, true)) {
                ItemStack helmetStack = player.getItemBySlot(EquipmentSlotType.HEAD);
                Item searchedItem = ForgeRegistries.ITEMS.getValue(itemId);
                if (searchedItem != null && searchedItem != Items.AIR) {
                    ItemPneumaticArmor.setSearchedItem(helmetStack, searchedItem);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
