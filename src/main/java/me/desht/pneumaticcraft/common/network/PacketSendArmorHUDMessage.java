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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to get a message displayed on the Pneumatic Armor HUD
 */
public record PacketSendArmorHUDMessage(Component message, int duration, int color) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("send_armor_hud_message");

    public static PacketSendArmorHUDMessage fromNetwork(FriendlyByteBuf buffer) {
        return new PacketSendArmorHUDMessage(buffer.readComponent(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(this.message);
        buf.writeInt(this.duration);
        buf.writeInt(this.color);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSendArmorHUDMessage message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() ->
                HUDHandler.getInstance().addMessage(new ArmorMessage(message.message(), message.duration(), message.color())));
    }
}
