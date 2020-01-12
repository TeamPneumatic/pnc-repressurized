package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = new DeferredRegister<>(ForgeRegistries.PARTICLE_TYPES, Names.MOD_ID);

    public static final RegistryObject<ParticleType<AirParticleData>> AIR_PARTICLE = register("air_particle",
            () -> new ParticleType<>(false, AirParticleData.DESERIALIZER));
    public static final RegistryObject<ParticleType<AirParticleData>> AIR_PARTICLE_2 = register("air_particle_2",
            () -> new ParticleType<>(false, AirParticleData.DESERIALIZER));

    private static <T extends ParticleType<?>> RegistryObject<T> register(String name, Supplier<T> sup) {
        return PARTICLES.register(name, sup);
    }
}
