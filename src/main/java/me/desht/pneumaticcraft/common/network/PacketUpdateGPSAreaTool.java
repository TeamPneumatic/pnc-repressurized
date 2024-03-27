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

import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client from area tool GUI to update stored settings
 */
public record PacketUpdateGPSAreaTool(CompoundTag areaWidgetData, InteractionHand hand) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_gps_area_tool");

    public PacketUpdateGPSAreaTool(FriendlyByteBuf buffer) {
        this(buffer.readNbt(), buffer.readEnum(InteractionHand.class));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeNbt(areaWidgetData);
        buffer.writeEnum(InteractionHand.MAIN_HAND);
    }

    public static void handle(PacketUpdateGPSAreaTool message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
                ItemStack stack = player.getItemInHand(message.hand);
                if (stack.getItem() == ModItems.GPS_AREA_TOOL.get()) {
                    stack.setTag(message.areaWidgetData);
                }
            })
        );
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
