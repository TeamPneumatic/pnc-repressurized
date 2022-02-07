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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to play a sound at a specific location
 */
public class PacketPlaySound extends LocationDoublePacket {
    private final SoundEvent soundEvent;
    private final SoundSource category;
    private final float volume;
    private final float pitch;
    private final boolean distanceDelay;

    public PacketPlaySound(SoundEvent soundEvent, SoundSource category, double x, double y, double z, float volume, float pitch, boolean distanceDelay) {
        super(x, y, z);
        this.soundEvent = soundEvent;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.distanceDelay = distanceDelay;
    }

    public PacketPlaySound(SoundEvent soundEvent, SoundSource category, BlockPos pos, float volume, float pitch, boolean distanceDelay) {
        this(soundEvent, category, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, distanceDelay);
    }

    public PacketPlaySound(FriendlyByteBuf buffer) {
        super(buffer);
        soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(buffer.readResourceLocation());
        category = SoundSource.values()[buffer.readInt()];
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
        distanceDelay = buffer.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeResourceLocation(Objects.requireNonNull(soundEvent.getRegistryName()));
        buffer.writeInt(category.ordinal());
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(distanceDelay);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientUtils.getClientLevel().playLocalSound(x, y, z, soundEvent, category, volume, pitch, distanceDelay));
        ctx.get().setPacketHandled(true);
    }
}
