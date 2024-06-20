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

package me.desht.pneumaticcraft.common.thirdparty.toughasnails;

import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.AirConClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when air conditioning level changes so client can update the HUD gauge
 */
public record PacketPlayerTemperatureDelta(int deltaTemp) implements CustomPacketPayload {
    public static final Type<PacketPlayerTemperatureDelta> TYPE = new Type<>(RL("player_temp_delta"));

    public static final StreamCodec<FriendlyByteBuf, PacketPlayerTemperatureDelta> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketPlayerTemperatureDelta::deltaTemp,
            PacketPlayerTemperatureDelta::new
    );

    private static final ResourceLocation ID = RL("player_temperature_delta");

    public static PacketPlayerTemperatureDelta fromNetwork(FriendlyByteBuf buffer) {
        return new PacketPlayerTemperatureDelta(buffer.readByte());
    }

    public static void handle(PacketPlayerTemperatureDelta message, IPayloadContext ctx) {
        AirConClientHandler.deltaTemp = message.deltaTemp();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
