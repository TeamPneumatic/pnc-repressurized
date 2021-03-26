package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to play a trail of particles between two points
 */
public class PacketSpawnParticleTrail extends LocationDoublePacket {
    private static final int DENSITY = 15;

    private final IParticleData particle;
    private final double x2;
    private final double y2;
    private final double z2;

    public PacketSpawnParticleTrail(IParticleData particle, double x1, double y1, double z1, double x2, double y2, double z2) {
        super(x1, y1, z1);
        this.particle = particle;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public PacketSpawnParticleTrail(PacketBuffer buffer) {
        super(buffer);
        ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(buffer.readResourceLocation());
        assert type != null;
        x2 = buffer.readDouble();
        y2 = buffer.readDouble();
        z2 = buffer.readDouble();
        particle = readParticle(type, buffer);
    }

    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
        buffer.writeResourceLocation(Objects.requireNonNull(particle.getType().getRegistryName()));
        buffer.writeDouble(x2);
        buffer.writeDouble(y2);
        buffer.writeDouble(z2);
        particle.write(new PacketBuffer(buffer));
    }

    private <T extends IParticleData> T readParticle(ParticleType<T> type, PacketBuffer buffer) {
        return type.getDeserializer().read(type, buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtils.getClientWorld();
            int numParticles = (int) PneumaticCraftUtils.distBetween(x, y, z, x2, y2, z2) * 25;
            if (numParticles == 0) numParticles = 1;
            for (int i = 0; i <= numParticles; i++) {
                double pct = (double) i / numParticles;
                double px = MathHelper.lerp(pct, x, x2);
                double py = MathHelper.lerp(pct, y, y2);
                double pz = MathHelper.lerp(pct, z, z2);
                world.addParticle(particle, px + world.rand.nextDouble() * 0.2 - 0.1, py + world.rand.nextDouble() * 0.2 - 0.1, pz + world.rand.nextDouble() * 0.2 - 0.1, 0, 0, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
