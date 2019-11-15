package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to spawn a particle (with support for multiple particles in an random area around the target point)
 */
public class PacketSpawnParticle extends LocationDoublePacket {

    private IParticleData particle;
    private double dx;
    private double dy;
    private double dz;
    private int numParticles;
    private double rx, ry, rz;

    public PacketSpawnParticle() {
    }

    public PacketSpawnParticle(IParticleData particle, double x, double y, double z, double dx, double dy, double dz) {
        super(x, y, z);
        this.particle = particle;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.numParticles = 1;
        this.rx = this.ry = this.rz = 0d;
    }

    public PacketSpawnParticle(IParticleData particle, double x, double y, double z, double dx, double dy, double dz, int numParticles, double rx, double ry, double rz) {
        this(particle, x, y, z, dx, dy, dz);
        this.numParticles = numParticles;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public PacketSpawnParticle(PacketBuffer buffer) {
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
        }
        particle = readParticle(type, buffer);
    }

    private <T extends IParticleData> T readParticle(ParticleType<T> type, PacketBuffer buffer) {
        return type.getDeserializer().read(type, buffer);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);

        buffer.writeResourceLocation(particle.getType().getRegistryName());
        buffer.writeDouble(dx);
        buffer.writeDouble(dy);
        buffer.writeDouble(dz);
        buffer.writeInt(numParticles);
        if (numParticles > 1) {
            buffer.writeDouble(rx);
            buffer.writeDouble(ry);
            buffer.writeDouble(rz);
        }
        particle.write(new PacketBuffer(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = PneumaticCraftRepressurized.proxy.getClientWorld();
            for (int i = 0; i < numParticles; i++) {
                double x1 = x + (numParticles == 1 ? 0 : world.rand.nextDouble() * rx);
                double y1 = y + (numParticles == 1 ? 0 : world.rand.nextDouble() * ry);
                double z1 = z + (numParticles == 1 ? 0 : world.rand.nextDouble() * rz);
                world.addParticle(particle, x1, y1, z1, dx, dy, dz);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
