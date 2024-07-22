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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client to update the searched item (Pneumatic Helmet search upgrade)
 */
public record PacketUpdateSearchItem(Item item) implements CustomPacketPayload {
    public static final Type<PacketUpdateSearchItem> TYPE = new Type<>(RL("search_item"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateSearchItem> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), PacketUpdateSearchItem::item,
            PacketUpdateSearchItem::new
    );

    @Override
    public Type<PacketUpdateSearchItem> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateSearchItem message, IPayloadContext ctx) {
        if (message.item() != null && message.item() != Items.AIR) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(ctx.player());
            if (handler.upgradeUsable(CommonUpgradeHandlers.searchHandler, true)) {
                ItemStack helmetStack = handler.getPlayer().getItemBySlot(EquipmentSlot.HEAD);
                if (helmetStack.getItem() instanceof PneumaticArmorItem) {  // should be, but let's be paranoid...
                    PneumaticArmorItem.setSearchedItem(helmetStack, message.item());
                }
            }
        }
    }
}
