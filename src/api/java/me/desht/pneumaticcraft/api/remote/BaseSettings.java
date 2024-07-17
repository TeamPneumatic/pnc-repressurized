package me.desht.pneumaticcraft.api.remote;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BaseSettings(String enableVariable, BlockPos enablingValue) {
    public static final Codec<BaseSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.optionalFieldOf("enable_var", "").forGetter(BaseSettings::enableVariable),
            BlockPos.CODEC.optionalFieldOf("enable_pos", BlockPos.ZERO).forGetter(BaseSettings::enablingValue)
    ).apply(builder, BaseSettings::new));
    public static final StreamCodec<FriendlyByteBuf, BaseSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BaseSettings::enableVariable,
            BlockPos.STREAM_CODEC, BaseSettings::enablingValue,
            BaseSettings::new
    );
    
    public static final BaseSettings DEFAULT = new BaseSettings("", BlockPos.ZERO);

    public BaseSettings withVariable(String var) {
        return new BaseSettings(var, enablingValue);
    }

    public BaseSettings withEnablingValue(BlockPos value) {
        return new BaseSettings(enableVariable, value);
    }
}
