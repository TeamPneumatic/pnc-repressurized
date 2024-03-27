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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update the searched item (Pneumatic Helmet search upgrade)
 */
public record PacketUpdateSearchItem(Item item) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("search_item");

    public static PacketUpdateSearchItem fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateSearchItem(buffer.readById(BuiltInRegistries.ITEM));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.ITEM, item);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateSearchItem message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            if (message.item() != null && message.item() != Items.AIR) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.upgradeUsable(CommonUpgradeHandlers.searchHandler, true)) {
                    ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmetStack.getItem() instanceof PneumaticArmorItem) {  // should be, but let's be paranoid...
                        PneumaticArmorItem.setSearchedItem(helmetStack, message.item());
                    }
                }
            }
        }));
    }
}
