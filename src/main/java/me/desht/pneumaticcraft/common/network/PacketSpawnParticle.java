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
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server to spawn a particle (with support for multiple particles in an random area around the target point)
 */
public record PacketSpawnParticle(ParticleOptions particle, float x, float y, float z, float dx, float dy, float dz, int numParticles, float rx, float ry, float rz) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("spawn_particle");

    public PacketSpawnParticle(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz, int numParticles, double rx, double ry, double rz) {
        this(particle, (float) x, (float) y, (float) z, (float) dx, (float) dy, (float) dz, numParticles, (float) rx, (float) ry, (float) rz);
    }

    public PacketSpawnParticle(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
        this(particle, (float) x, (float) y, (float) z, (float) dx, (float) dy, (float) dz, 1, 0f, 0f, 0f);
    }

    public static PacketSpawnParticle fromNetwork(FriendlyByteBuf buffer) {
        ParticleType<?> type = buffer.readById(BuiltInRegistries.PARTICLE_TYPE);
        assert type != null;
        float x = buffer.readFloat();
        float y = buffer.readFloat();
        float z = buffer.readFloat();
        float dx = buffer.readFloat();
        float dy = buffer.readFloat();
        float dz = buffer.readFloat();
        int numParticles = buffer.readVarInt();
        float rx, ry, rz;
        if (numParticles > 1) {
            rx = buffer.readFloat();
            ry = buffer.readFloat();
            rz = buffer.readFloat();
        } else {
            rx = ry = rz = 0f;
        }
        ParticleOptions particle = readParticle(type, buffer);

        return new PacketSpawnParticle(particle, x, y, z, dx, dy, dz, numParticles, rx, ry, rz);
    }

    private static <T extends ParticleOptions> T readParticle(ParticleType<T> type, FriendlyByteBuf buffer) {
        return type.getDeserializer().fromNetwork(type, buffer);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeId(BuiltInRegistries.PARTICLE_TYPE, particle.getType());
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(dx);
        buffer.writeFloat(dy);
        buffer.writeFloat(dz);
        buffer.writeVarInt(numParticles);
        if (numParticles > 1) {
            buffer.writeFloat(rx);
            buffer.writeFloat(ry);
            buffer.writeFloat(rz);
        }
        particle.writeToNetwork(new FriendlyByteBuf(buffer));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSpawnParticle message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            Level world = ClientUtils.getClientLevel();
            int numParticles = message.numParticles();
            for (int i = 0; i < numParticles; i++) {
                double x1 = message.x() + (numParticles == 1 ? 0 : world.random.nextDouble() * message.rx());
                double y1 = message.y() + (numParticles == 1 ? 0 : world.random.nextDouble() * message.ry());
                double z1 = message.z() + (numParticles == 1 ? 0 : world.random.nextDouble() * message.rz());
                world.addParticle(message.particle(), x1, y1, z1, message.dx(), message.dy(), message.dz());
            }
        });
    }
}
