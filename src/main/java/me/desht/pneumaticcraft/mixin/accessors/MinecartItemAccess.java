package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.MinecartItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartItem.class)
public interface MinecartItemAccess {
    @Accessor
    EntityType<? extends AbstractMinecart> getType();

}