package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccess {
    @Accessor
    float[] getArmorDropChances();

    @Accessor
    float[] getHandDropChances();

}