package me.desht.pneumaticcraft.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.common.core.ModParticleTypes;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

import java.util.Calendar;
import java.util.Locale;

public class AirParticleData implements IParticleData {
    public static final AirParticleData NORMAL = new AirParticleData(0.1f);
    public static final AirParticleData DENSE = new AirParticleData(0.1f);
    public static final IDeserializer<AirParticleData> DESERIALIZER = new IDeserializer<AirParticleData>() {
        @Override
        public AirParticleData deserialize(ParticleType<AirParticleData> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float alpha = stringReader.readFloat();
            return new AirParticleData(alpha);
        }

        @Override
        public AirParticleData read(ParticleType<AirParticleData> particleType, PacketBuffer packetBuffer) {
            return new AirParticleData(packetBuffer.readFloat());
        }
    };

    private static boolean checkDate;
    private static boolean useAlt;

    private final float alpha;

    public AirParticleData(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public ParticleType<?> getType() {
        return useAltParticles() ? ModParticleTypes.AIR_PARTICLE_2 : ModParticleTypes.AIR_PARTICLE;
    }

    @Override
    public void write(PacketBuffer packetBuffer) {
        packetBuffer.writeFloat(alpha);
    }

    @Override
    public String getParameters() {
        return String.format(Locale.ROOT, "%s %f", getType().getRegistryName(), alpha);
    }

    float getAlpha() {
        return alpha;
    }

    private boolean useAltParticles() {
        if (!checkDate) {
            Calendar calendar = Calendar.getInstance();
            useAlt = calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) >= 31
                    || calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) <= 2;
            checkDate = true;
        }
        return useAlt;
    }
}
