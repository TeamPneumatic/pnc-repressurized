package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public class PacketSpawnParticle extends LocationDoublePacket<PacketSpawnParticle> {

    private double dx, dy, dz;
    private int particleId;
    private int numParticles;
    private double rx, ry, rz;

    public PacketSpawnParticle() {
    }

    public PacketSpawnParticle(EnumParticleTypes particle, double x, double y, double z, double dx, double dy, double dz) {
        super(x, y, z);
        particleId = particle.ordinal();
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.numParticles = 1;
        this.rx = this.ry = this.rz = 0d;
    }

    public PacketSpawnParticle(EnumParticleTypes particle, double x, double y, double z, double dx, double dy, double dz, int numParticles, double rx, double ry, double rz) {
        super(x, y, z);
        particleId = particle.ordinal();
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.numParticles = numParticles;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(particleId);
        buffer.writeDouble(dx);
        buffer.writeDouble(dy);
        buffer.writeDouble(dz);
        buffer.writeInt(numParticles);
        if (numParticles > 1) {
            buffer.writeDouble(rx);
            buffer.writeDouble(ry);
            buffer.writeDouble(rz);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        particleId = buffer.readInt();
        dx = buffer.readDouble();
        dy = buffer.readDouble();
        dz = buffer.readDouble();
        numParticles = buffer.readInt();
        if (numParticles > 1) {
            rx = buffer.readDouble();
            ry = buffer.readDouble();
            rz = buffer.readDouble();
        }
    }

    @Override
    public void handleClientSide(PacketSpawnParticle message, EntityPlayer player) {
        for (int i = 0; i < numParticles; i++) {
            double x = message.x + (numParticles == 1 ? 0 : player.world.rand.nextDouble() * rx);
            double y = message.y + (numParticles == 1 ? 0 :  player.world.rand.nextDouble() * ry);
            double z = message.z + (numParticles == 1 ? 0 : player.world.rand.nextDouble() * rz);
            player.world.spawnParticle(EnumParticleTypes.values()[message.particleId], x, y, z, message.dx, message.dy, message.dz);
        }
    }

    @Override
    public void handleServerSide(PacketSpawnParticle message, EntityPlayer player) {
    }

}
