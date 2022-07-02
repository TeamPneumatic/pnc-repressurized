package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccess {
    @Accessor
    int getBoardingCooldown();

    @Accessor
    void setBoardingCooldown(int boardingCooldown);
}