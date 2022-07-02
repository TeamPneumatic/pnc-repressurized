package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccess {
    @Accessor
    int getLitTime();

    @Accessor
    void setLitTime(int litTime);

    @Accessor
    void setLitDuration(int litDuration);

    @Accessor
    int getCookingProgress();

}