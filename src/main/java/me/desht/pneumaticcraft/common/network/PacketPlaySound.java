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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to play a sound at a specific location
 */
public record PacketPlaySound(SoundEvent soundEvent, SoundSource category, float x, float y, float z, float volume, float pitch, boolean distanceDelay) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("play_sound");

    public PacketPlaySound(SoundEvent soundEvent, SoundSource category, BlockPos pos, float volume, float pitch, boolean distanceDelay) {
        this(soundEvent, category, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, distanceDelay);
    }

    public PacketPlaySound(FriendlyByteBuf buffer) {
        this(buffer.readById(BuiltInRegistries.SOUND_EVENT), buffer.readEnum(SoundSource.class),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.SOUND_EVENT, soundEvent);
        buffer.writeEnum(category);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(distanceDelay);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketPlaySound message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> ClientUtils.getClientLevel().playLocalSound(
                message.x, message.y, message.z,
                message.soundEvent, message.category,
                message.volume, message.pitch, message.distanceDelay
        ));
    }
}
