package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BoatItem.class)
public interface BoatItemAccess {
    @Invoker("getBoat")
    Boat invokeGetBoat(Level level, HitResult hitResult);
}
