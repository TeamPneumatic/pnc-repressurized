package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityAccess {
    @Accessor
    int getAge();

    @Accessor
    void setAge(int age);
}
