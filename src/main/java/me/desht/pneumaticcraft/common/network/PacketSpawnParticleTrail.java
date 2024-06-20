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

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to play a trail of particles between two points
 */
public record PacketSpawnParticleTrail(ParticleOptions particle, Vector3f start, Vector3f end) implements CustomPacketPayload {
    public static final Type<PacketSpawnParticleTrail> TYPE = new Type<>(RL("particle_trail"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSpawnParticleTrail> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, PacketSpawnParticleTrail::particle,
            ByteBufCodecs.VECTOR3F, PacketSpawnParticleTrail::start,
            ByteBufCodecs.VECTOR3F, PacketSpawnParticleTrail::end,
            PacketSpawnParticleTrail::new
    );

    @Override
    public Type<PacketSpawnParticleTrail> type() {
        return TYPE;
    }

    public static void handle(PacketSpawnParticleTrail message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        int numParticles = Math.max(1, (int) message.start.distance(message.end) * 25);

        for (int i = 0; i <= numParticles; i++) {
            double pct = (double) i / numParticles;
            double px = Mth.lerp(pct, message.start.x(), message.end.x());
            double py = Mth.lerp(pct, message.start.y(), message.end.y());
            double pz = Mth.lerp(pct, message.start.z(), message.end.z());
            level.addParticle(message.particle(),
                    px + level.random.nextDouble() * 0.2 - 0.1,
                    py + level.random.nextDouble() * 0.2 - 0.1,
                    pz + level.random.nextDouble() * 0.2 - 0.1,
                    0, 0, 0);
        }
    }
}
