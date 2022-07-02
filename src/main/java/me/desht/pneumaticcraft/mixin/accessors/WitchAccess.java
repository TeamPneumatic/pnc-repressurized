package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Witch.class)
public interface WitchAccess {
    @Accessor
    void setUsingTime(int usingTime);

}