package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccess {
    @Accessor
    SpawnData getNextSpawnData();

    @Accessor
    int getRequiredPlayerRange();

    @Accessor
    void setRequiredPlayerRange(int range);

    @Accessor
    int getSpawnDelay();

    @Accessor
    void setSpawnDelay(int delay);

    @Accessor
    void setOSpin(double spin);

    @Accessor
    double getSpin();
}
