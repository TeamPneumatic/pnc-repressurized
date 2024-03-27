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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.particle.AirParticleType;
import me.desht.pneumaticcraft.common.particle.BulletParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES
            = DeferredRegister.create(Registries.PARTICLE_TYPE, Names.MOD_ID);

    public static final Supplier<ParticleType<AirParticleData>> AIR_PARTICLE
            = register("air_particle", AirParticleType::new);
    public static final Supplier<ParticleType<AirParticleData>> AIR_PARTICLE_2
            = register("air_particle_2", AirParticleType::new);
    public static final Supplier<SimpleParticleType> BULLET_PARTICLE
            = register("bullet_particle", BulletParticleType::new);

    private static <T extends ParticleType<?>> Supplier<T> register(String name, Supplier<T> sup) {
        return PARTICLES.register(name, sup);
    }
}
