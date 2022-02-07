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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to spawn a particle (with support for multiple particles in an random area around the target point)
 */
public class PacketSpawnParticle extends LocationDoublePacket {
    private final ParticleOptions particle;
    private final double dx;
    private final double dy;
    private final double dz;
    private final int numParticles;
    private final double rx, ry, rz;

    public PacketSpawnParticle(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
        this(particle, x, y, z, dx, dy, dz, 1, 0, 0, 0);
    }

    public PacketSpawnParticle(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz, int numParticles, double rx, double ry, double rz) {
        super(x, y, z);
        this.particle = particle;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.numParticles = numParticles;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public PacketSpawnParticle(FriendlyByteBuf buffer) {
        super(buffer);
        ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(buffer.readResourceLocation());
        assert type != null;
        dx = buffer.readDouble();
        dy = buffer.readDouble();
        dz = buffer.readDouble();
        numParticles = buffer.readInt();
        if (numParticles > 1) {
            rx = buffer.readDouble();
            ry = buffer.readDouble();
            rz = buffer.readDouble();
        } else {
            rx = ry = rz = 0;
        }
        particle = readParticle(type, buffer);
    }

    private <T extends ParticleOptions> T readParticle(ParticleType<T> type, FriendlyByteBuf buffer) {
        return type.getDeserializer().fromNetwork(type, buffer);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeResourceLocation(Objects.requireNonNull(particle.getType().getRegistryName()));
        buffer.writeDouble(dx);
        buffer.writeDouble(dy);
        buffer.writeDouble(dz);
        buffer.writeInt(numParticles);
        if (numParticles > 1) {
            buffer.writeDouble(rx);
            buffer.writeDouble(ry);
            buffer.writeDouble(rz);
        }
        particle.writeToNetwork(new FriendlyByteBuf(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Level world = ClientUtils.getClientLevel();
            for (int i = 0; i < numParticles; i++) {
                double x1 = x + (numParticles == 1 ? 0 : world.random.nextDouble() * rx);
                double y1 = y + (numParticles == 1 ? 0 : world.random.nextDouble() * ry);
                double z1 = z + (numParticles == 1 ? 0 : world.random.nextDouble() * rz);
                world.addParticle(particle, x1, y1, z1, dx, dy, dz);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
