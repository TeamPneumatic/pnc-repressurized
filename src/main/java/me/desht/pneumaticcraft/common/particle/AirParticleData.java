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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.registry.ModParticleTypes;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Calendar;
import java.util.Locale;

public class AirParticleData implements ParticleOptions {
    public static final AirParticleData NORMAL = new AirParticleData(0.1f);
    public static final AirParticleData DENSE = new AirParticleData(0.3f);

    public static final Deserializer<AirParticleData> DESERIALIZER = new Deserializer<AirParticleData>() {
        @Override
        public AirParticleData fromCommand(ParticleType<AirParticleData> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float alpha = stringReader.readFloat();
            return new AirParticleData(alpha);
        }

        @Override
        public AirParticleData fromNetwork(ParticleType<AirParticleData> particleType, FriendlyByteBuf packetBuffer) {
            return new AirParticleData(packetBuffer.readFloat());
        }
    };
    static final Codec<AirParticleData> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(Codec.FLOAT.fieldOf("alpha")
                    .forGetter((d) -> d.alpha))
                    .apply(instance, AirParticleData::new));

    private static boolean checkedDate;
    private static boolean useAlt;

    private final float alpha;

    public AirParticleData(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public ParticleType<?> getType() {
        return useAltParticles() ? ModParticleTypes.AIR_PARTICLE_2.get() : ModParticleTypes.AIR_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeFloat(alpha);
    }

    @Override
    public String writeToString() {
        ResourceLocation regName = PneumaticCraftUtils.getRegistryName(BuiltInRegistries.PARTICLE_TYPE, getType()).orElseThrow();
        return String.format(Locale.ROOT, "%s %f", regName, alpha);
    }

    public float getAlpha() {
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
