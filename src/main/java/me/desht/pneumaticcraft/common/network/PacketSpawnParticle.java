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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import java.util.Optional;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to spawn a particle (with support for multiple particles in a random area around the target point)
 */
public record PacketSpawnParticle(ParticleOptions particle, Vector3f pos, Vector3f speed, int numParticles, Optional<Vector3f> randomOffset) implements CustomPacketPayload {
    public static final Type<PacketSpawnParticle> TYPE = new Type<>(RL("spawn_particle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSpawnParticle> STREAM_CODEC = StreamCodec.composite(
            ParticleTypes.STREAM_CODEC, PacketSpawnParticle::particle,
            ByteBufCodecs.VECTOR3F, PacketSpawnParticle::pos,
            ByteBufCodecs.VECTOR3F, PacketSpawnParticle::speed,
            ByteBufCodecs.VAR_INT, PacketSpawnParticle::numParticles,
            ByteBufCodecs.optional(ByteBufCodecs.VECTOR3F), PacketSpawnParticle::randomOffset,
            PacketSpawnParticle::new
    );

    public static PacketSpawnParticle oneParticle(ParticleOptions particle, Vector3f pos, Vector3f speed) {
        return new PacketSpawnParticle(particle, pos, speed, 1, Optional.empty());
    }

    @Override
    public Type<PacketSpawnParticle> type() {
        return TYPE;
    }

    public static void handle(PacketSpawnParticle message, IPayloadContext ctx) {
        Level world = ClientUtils.getClientLevel();
        int numParticles = message.numParticles();
        Vector3f r = message.randomOffset.orElse(new Vector3f(0f, 0f, 0f));
        for (int i = 0; i < numParticles; i++) {
            double x1 = message.pos.x + (numParticles == 1 ? 0 : world.random.nextDouble() * r.x);
            double y1 = message.pos.y + (numParticles == 1 ? 0 : world.random.nextDouble() * r.y);
            double z1 = message.pos.z + (numParticles == 1 ? 0 : world.random.nextDouble() * r.z);
            world.addParticle(message.particle(), x1, y1, z1, message.speed.x, message.speed.y, message.speed.z);
        }
    }
}
