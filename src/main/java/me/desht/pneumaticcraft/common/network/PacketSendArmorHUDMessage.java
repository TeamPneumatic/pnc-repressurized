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

import me.desht.pneumaticcraft.client.render.pneumatic_armor.ArmorMessage;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to get a message displayed on the Pneumatic Armor HUD
 */
public record PacketSendArmorHUDMessage(Component message, int duration, int color) implements CustomPacketPayload {
    public static final Type<PacketSendArmorHUDMessage> TYPE = new Type<>(RL("send_armor_hud_message"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSendArmorHUDMessage> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, PacketSendArmorHUDMessage::message,
            ByteBufCodecs.VAR_INT, PacketSendArmorHUDMessage::duration,
            ByteBufCodecs.INT, PacketSendArmorHUDMessage::color,
            PacketSendArmorHUDMessage::new
    );

    @Override
    public Type<PacketSendArmorHUDMessage> type() {
        return TYPE;
    }

    public static void handle(PacketSendArmorHUDMessage message, IPayloadContext ctx) {
        HUDHandler.getInstance().addMessage(new ArmorMessage(message.message(), message.duration(), message.color()));
    }
}
