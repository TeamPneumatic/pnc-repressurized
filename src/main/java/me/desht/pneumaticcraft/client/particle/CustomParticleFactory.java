package me.desht.pneumaticcraft.client.particle;

import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class CustomParticleFactory {
    public static Particle createParticle(EnumCustomParticleType particleType, World w, double x, double y, double z, double dx, double dy, double dz) {
        switch (particleType) {
            case AIR_PARTICLE:
                return new AirParticle(w, x, y, z, dx, dy, dz);
            case AIR_PARTICLE_DENSE:
                AirParticle p = new AirParticle(w, x, y, z, dx, dy, dz);
                p.setAlphaF(0.3f);
                return p;
            default:
                return null;
        }
    }
}
