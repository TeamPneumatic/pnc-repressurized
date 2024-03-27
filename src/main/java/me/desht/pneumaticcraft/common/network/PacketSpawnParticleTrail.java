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
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to play a trail of particles between two points
 */
public record PacketSpawnParticleTrail(ParticleOptions particle, float x, float y, float z, float x2, float y2, float z2) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("particle_trail");

    public PacketSpawnParticleTrail(ParticleOptions particle, double x, double y, double z, double x2, double y2, double z2) {
        this(particle, (float) x, (float) y, (float) z, (float) x2, (float) y2, (float) z2);
    }

    public static PacketSpawnParticleTrail fromNetwork(FriendlyByteBuf buffer) {
        ParticleType<?> type = buffer.readById(BuiltInRegistries.PARTICLE_TYPE);
        assert type != null;
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();
        float x2 = buffer.readFloat();
        float y2 = buffer.readFloat();
        float z2 = buffer.readFloat();
        ParticleOptions particle = readParticle(type, buffer);

        return new PacketSpawnParticleTrail(particle, x, y, z, x2, y2, z2);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.PARTICLE_TYPE, particle.getType());
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(x2);
        buffer.writeFloat(y2);
        buffer.writeFloat(z2);
        particle.writeToNetwork(new FriendlyByteBuf(buffer));
    }

    private static <T extends ParticleOptions> T readParticle(ParticleType<T> type, FriendlyByteBuf buffer) {
        return type.getDeserializer().fromNetwork(type, buffer);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSpawnParticleTrail message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Level world = ClientUtils.getClientLevel();
            int numParticles = (int) PneumaticCraftUtils.distBetween(message.x(), message.y(), message.z(), message.x2(), message.y2(), message.z2()) * 25;
            if (numParticles == 0) numParticles = 1;
            for (int i = 0; i <= numParticles; i++) {
                double pct = (double) i / numParticles;
                double px = Mth.lerp(pct, message.x(), message.x2());
                double py = Mth.lerp(pct, message.y(), message.y2());
                double pz = Mth.lerp(pct, message.z(), message.z2());
                world.addParticle(message.particle(),
                        px + world.random.nextDouble() * 0.2 - 0.1,
                        py + world.random.nextDouble() * 0.2 - 0.1,
                        pz + world.random.nextDouble() * 0.2 - 0.1,
                        0, 0, 0);
            }
        });
    }
}
