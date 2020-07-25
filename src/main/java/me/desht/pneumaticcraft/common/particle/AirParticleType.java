package me.desht.pneumaticcraft.common.particle;

import com.mojang.serialization.Codec;
import net.minecraft.particles.ParticleType;

public class AirParticleType extends ParticleType<AirParticleData> {
    public AirParticleType() {
        super(false, AirParticleData.DESERIALIZER);
    }

    @Override
    public Codec<AirParticleData> func_230522_e_() {
        return AirParticleData.CODEC;
    }
}
