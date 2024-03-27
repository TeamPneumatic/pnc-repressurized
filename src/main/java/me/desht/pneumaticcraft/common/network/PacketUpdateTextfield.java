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

import me.desht.pneumaticcraft.common.block.entity.IGUITextFieldSensitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client GUI's to update a IGUITextFieldSensitive block entity server-side
 */
public record PacketUpdateTextfield(int textFieldID, String text) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_textfield");

    public static PacketUpdateTextfield create(IGUITextFieldSensitive sensitive, int id) {
        return new PacketUpdateTextfield(id, sensitive.getText(id));
    }

    public static PacketUpdateTextfield fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateTextfield(buffer.readVarInt(), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(textFieldID);
        buffer.writeUtf(text);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateTextfield message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() ->
                PacketUtil.getBlockEntity(player, BlockEntity.class).ifPresent(te -> {
                    if (te instanceof IGUITextFieldSensitive sensitive) {
                        sensitive.setText(message.textFieldID(), message.text());
                    }
                })
        ));
    }
}
