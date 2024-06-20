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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client GUI's to update a IGUITextFieldSensitive block entity server-side
 */
public record PacketUpdateTextfield(int textFieldID, String text) implements CustomPacketPayload {
    public static final Type<PacketUpdateTextfield> TYPE = new Type<>(RL("update_textfield"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateTextfield> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PacketUpdateTextfield::textFieldID,
            ByteBufCodecs.STRING_UTF8, PacketUpdateTextfield::text,
            PacketUpdateTextfield::new
    );

    public static PacketUpdateTextfield create(IGUITextFieldSensitive sensitive, int id) {
        return new PacketUpdateTextfield(id, sensitive.getText(id));
    }

    @Override
    public Type<PacketUpdateTextfield> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateTextfield message, IPayloadContext ctx) {
        PacketUtil.getBlockEntity(ctx.player(), BlockEntity.class).ifPresent(te -> {
            if (te instanceof IGUITextFieldSensitive sensitive) {
                sensitive.setText(message.textFieldID(), message.text());
            }
        });
    }
}
