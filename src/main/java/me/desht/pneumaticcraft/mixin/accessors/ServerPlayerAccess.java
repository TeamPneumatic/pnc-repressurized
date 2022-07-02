package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccess {
    @Accessor
    void setIsChangingDimension(boolean isChangingDimension);

}