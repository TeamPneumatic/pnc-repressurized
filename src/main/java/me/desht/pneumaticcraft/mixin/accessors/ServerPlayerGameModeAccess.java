package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface ServerPlayerGameModeAccess {
    @Accessor("isDestroyingBlock")
    boolean isDestroyingBlock();

    @Accessor("hasDelayedDestroy")
    boolean hasDelayedDestroy();

}