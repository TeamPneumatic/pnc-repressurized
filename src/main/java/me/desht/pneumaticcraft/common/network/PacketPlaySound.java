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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to play a sound at a specific location
 */
public record PacketPlaySound(SoundEvent soundEvent, SoundSource category, float x, float y, float z, float volume, float pitch, boolean distanceDelay) implements CustomPacketPayload {
    public static final Type<PacketPlaySound> TYPE = new Type<>(RL("play_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketPlaySound> STREAM_CODEC = StreamCodec.of(
            PacketPlaySound::toNetwork,
            PacketPlaySound::fromNetwork
    );

    public PacketPlaySound(SoundEvent soundEvent, SoundSource category, BlockPos pos, float volume, float pitch, boolean distanceDelay) {
        this(soundEvent, category, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, distanceDelay);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, PacketPlaySound packet) {
        SoundEvent.STREAM_CODEC.encode(buffer, Holder.direct(packet.soundEvent));
        buffer.writeEnum(packet.category);
        buffer.writeFloat(packet.x);
        buffer.writeFloat(packet.y);
        buffer.writeFloat(packet.z);
        buffer.writeFloat(packet.volume);
        buffer.writeFloat(packet.pitch);
        buffer.writeBoolean(packet.distanceDelay);
    }

    private static PacketPlaySound fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new PacketPlaySound(SoundEvent.STREAM_CODEC.decode(buffer).value(), buffer.readEnum(SoundSource.class),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    @Override
    public Type<PacketPlaySound> type() {
        return TYPE;
    }

    public static void handle(PacketPlaySound message, IPayloadContext ctx) {
        ctx.player().level().playLocalSound(
                message.x, message.y, message.z,
                message.soundEvent, message.category,
                message.volume, message.pitch, message.distanceDelay
        );
    }
}
