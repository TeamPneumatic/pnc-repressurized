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

import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.drone.progwidgets.SavedDroneProgram;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client from area tool GUI to update stored settings
 */
public record PacketUpdateGPSAreaTool(ProgWidgetArea widget, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<PacketUpdateGPSAreaTool> TYPE = new Type<>(RL("update_gps_area_tool"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateGPSAreaTool> STREAM_CODEC = StreamCodec.composite(
            ProgWidgetArea.STREAM_CODEC, PacketUpdateGPSAreaTool::widget,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketUpdateGPSAreaTool::hand,
            PacketUpdateGPSAreaTool::new
    );

    public static void handle(PacketUpdateGPSAreaTool message, IPayloadContext ctx) {
        ItemStack stack = ctx.player().getItemInHand(message.hand);
        if (stack.getItem() == ModItems.GPS_AREA_TOOL.get()) {
            GPSAreaToolItem.setArea(stack, message.widget);
        }
    }

    @Override
    public Type<PacketUpdateGPSAreaTool> type() {
        return TYPE;
    }
}
