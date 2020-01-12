package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = new DeferredRegister<>(ForgeRegistries.ENTITIES, Names.MOD_ID);

    public static final RegistryObject<EntityType<EntityVortex>> VORTEX
            = register("vortex", ModEntities::vortex);
    public static final RegistryObject<EntityType<EntityDrone>> DRONE
            = register("drone", ModEntities::drone);
    public static final RegistryObject<EntityType<EntityLogisticsDrone>> LOGISTICS_DRONE
            = register("logistics_drone", ModEntities::logisticsDrone);
    public static final RegistryObject<EntityType<EntityHarvestingDrone>> HARVESTING_DRONE
            = register("harvesting_drone", ModEntities::harvestingDrone);
    public static final RegistryObject<EntityType<EntityMicromissile>> MICROMISSILE
            = register("micromissile", ModEntities::micromissile);
    public static final RegistryObject<EntityType<EntityTumblingBlock>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntities::tumblingBlock);
    public static final RegistryObject<EntityType<EntityRing>> RING
            = register("ring", ModEntities::ring);

    private static <E extends Entity> RegistryObject<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITIES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<EntityVortex> vortex() {
        return EntityType.Builder.create(EntityVortex::create, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(3)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.VORTEX.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityDrone> drone() {
        return EntityType.Builder.create(EntityDrone::create, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityLogisticsDrone> logisticsDrone() {
        return EntityType.Builder.create(EntityLogisticsDrone::createLogisticsDrone, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.LOGISTICS_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityHarvestingDrone> harvestingDrone() {
        return EntityType.Builder.create(EntityHarvestingDrone::createHarvestingDrone, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.HARVESTING_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityMicromissile> micromissile() {
        return EntityType.Builder.create(EntityMicromissile::create, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.MICROMISSILE.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityTumblingBlock> tumblingBlock() {
        return EntityType.Builder.create(EntityTumblingBlock::create, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.TUMBLING_BLOCK.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityRing> ring() {
        return EntityType.Builder.create(EntityRing::create, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.RING.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }
}
