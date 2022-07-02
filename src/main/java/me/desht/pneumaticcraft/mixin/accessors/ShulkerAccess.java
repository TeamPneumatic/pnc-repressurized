package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.monster.Shulker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Shulker.class)
public interface ShulkerAccess {
    @Invoker
    void callSetRawPeekAmount(int rawPeekAmount);

}