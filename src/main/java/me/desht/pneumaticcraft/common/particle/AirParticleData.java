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

package me.desht.pneumaticcraft.common.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Calendar;

public record AirParticleData(float alpha) implements ParticleOptions {
    public static final AirParticleData NORMAL = new AirParticleData(0.1f);
    public static final AirParticleData DENSE = new AirParticleData(0.3f);

    static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                    Codec.FLOAT.fieldOf("alpha").forGetter(AirParticleData::alpha)
            ).apply(builder, AirParticleData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, AirParticleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, AirParticleData::alpha,
            AirParticleData::new
    );

    private static boolean checkedDate;
    private static boolean useAlt;

    @Override
    public ParticleType<?> getType() {
        return useAltParticles() ? ModParticleTypes.AIR_PARTICLE_2.get() : ModParticleTypes.AIR_PARTICLE.get();
    }

    @Override
    public float alpha() {
        return useAlt ? alpha * 2 : alpha;
    }

    private boolean useAltParticles() {
        if (!checkedDate) {
            Calendar calendar = Calendar.getInstance();
            useAlt = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
            checkedDate = true;
        }
        return useAlt;
    }
}
