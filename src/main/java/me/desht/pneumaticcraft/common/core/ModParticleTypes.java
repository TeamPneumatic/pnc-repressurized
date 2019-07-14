package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Names.MOD_ID)
public class ModParticleTypes {
    public static final ParticleType<AirParticleData> AIR_PARTICLE = null;
    public static final ParticleType<AirParticleData> AIR_PARTICLE_2 = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerParticles(RegistryEvent.Register<ParticleType<?>> event) {
            event.getRegistry().register(new ParticleType<>(false, AirParticleData.DESERIALIZER).setRegistryName("air_particle"));
            event.getRegistry().register(new ParticleType<>(false, AirParticleData.DESERIALIZER).setRegistryName("air_particle_2"));
        }
    }
}
