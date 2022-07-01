package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenRegion.class)
public interface WorldGenRegionAccess {
    @Accessor
    StructureManager getStructureManager();
}
