package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.*;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.common.entity.semiblock.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Names.MOD_ID);

    public static final RegistryObject<EntityType<EntityDrone>> DRONE
            = register("drone", ModEntities::drone);
    public static final RegistryObject<EntityType<EntityLogisticsDrone>> LOGISTICS_DRONE
            = register("logistics_drone", ModEntities::logisticsDrone);
    public static final RegistryObject<EntityType<EntityHarvestingDrone>> HARVESTING_DRONE
            = register("harvesting_drone", ModEntities::harvestingDrone);
    public static final RegistryObject<EntityType<EntityGuardDrone>> GUARD_DRONE
            = register("guard_drone", ModEntities::guardDrone);
    public static final RegistryObject<EntityType<EntityCollectorDrone>> COLLECTOR_DRONE
            = register("collector_drone", ModEntities::collectorDrone);

    public static final RegistryObject<EntityType<EntityAmadrone>> AMADRONE
            = register("amadrone", ModEntities::amadrone);
    public static final RegistryObject<EntityType<EntityProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ModEntities::programmableController);

    public static final RegistryObject<EntityType<EntityVortex>> VORTEX
            = register("vortex", ModEntities::vortex);
    public static final RegistryObject<EntityType<EntityMicromissile>> MICROMISSILE
            = register("micromissile", ModEntities::micromissile);
    public static final RegistryObject<EntityType<EntityTumblingBlock>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntities::tumblingBlock);
    public static final RegistryObject<EntityType<EntityRing>> RING
            = register("ring", ModEntities::ring);

    public static final RegistryObject<EntityType<EntityCropSupport>> CROP_SUPPORT
            = register("crop_support", ModEntities::cropSupport);
    public static final RegistryObject<EntityType<EntitySpawnerAgitator>> SPAWNER_AGITATOR
            = register("spawner_agitator", ModEntities::spawnerAgitator);
    public static final RegistryObject<EntityType<EntityHeatFrame>> HEAT_FRAME
            = register("heat_frame", ModEntities::heatFrame);
    public static final RegistryObject<EntityType<EntityTransferGadget>> TRANSFER_GADGET
            = register("transfer_gadget", ModEntities::transferGadget);
    public static final RegistryObject<EntityType<EntityLogisticsActiveProvider>> LOGISTICS_FRAME_ACTIVE_PROVIDER
            = register("logistics_frame_active_provider", ModEntities::activeProvider);
    public static final RegistryObject<EntityType<EntityLogisticsPassiveProvider>> LOGISTICS_FRAME_PASSIVE_PROVIDER
            = register("logistics_frame_passive_provider", ModEntities::passiveProvider);
    public static final RegistryObject<EntityType<EntityLogisticsStorage>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ModEntities::storage);
    public static final RegistryObject<EntityType<EntityLogisticsDefaultStorage>> LOGISTICS_FRAME_DEFAULT_STORAGE
            = register("logistics_frame_default_storage", ModEntities::defaultStorage);
    public static final RegistryObject<EntityType<EntityLogisticsRequester>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ModEntities::requester);

    private static <E extends Entity> RegistryObject<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITIES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<EntityVortex> vortex() {
        return EntityType.Builder.create(EntityVortex::new, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(3)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.VORTEX.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityDrone> drone() {
        return EntityType.Builder.<EntityDrone>create(EntityDrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityAmadrone> amadrone() {
        return EntityType.Builder.create(EntityAmadrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.AMADRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityLogisticsDrone> logisticsDrone() {
        return EntityType.Builder.<EntityLogisticsDrone>create(EntityLogisticsDrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.LOGISTICS_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityHarvestingDrone> harvestingDrone() {
        return EntityType.Builder.<EntityHarvestingDrone>create(EntityHarvestingDrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.HARVESTING_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityGuardDrone> guardDrone() {
        return EntityType.Builder.<EntityGuardDrone>create(EntityGuardDrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.GUARD_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityCollectorDrone> collectorDrone() {
        return EntityType.Builder.<EntityCollectorDrone>create(EntityCollectorDrone::new, EntityClassification.CREATURE)
                .size(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.COLLECTOR_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityProgrammableController> programmableController() {
        return EntityType.Builder.create(EntityProgrammableController::new, EntityClassification.CREATURE)
                .size(0.35f, 0.175f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntities.PROGRAMMABLE_CONTROLLER.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityMicromissile> micromissile() {
        return EntityType.Builder.<EntityMicromissile>create(EntityMicromissile::new, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.MICROMISSILE.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityTumblingBlock> tumblingBlock() {
        return EntityType.Builder.<EntityTumblingBlock>create(EntityTumblingBlock::new, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.TUMBLING_BLOCK.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityRing> ring() {
        return EntityType.Builder.<EntityRing>create(EntityRing::new, EntityClassification.MISC)
                .size(0.5f, 0.5f)
                .immuneToFire()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.RING.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityCropSupport> cropSupport() {
        return EntityType.Builder.create(EntityCropSupport::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.CROP_SUPPORT.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntitySpawnerAgitator> spawnerAgitator() {
        return EntityType.Builder.create(EntitySpawnerAgitator::new, EntityClassification.MISC)
                .size(1F, 1F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.SPAWNER_AGITATOR.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityHeatFrame> heatFrame() {
        return EntityType.Builder.create(EntityHeatFrame::new, EntityClassification.MISC)
                .size(1F, 1F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.HEAT_FRAME.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityTransferGadget> transferGadget() {
        return EntityType.Builder.create(EntityTransferGadget::new, EntityClassification.MISC)
                .size(1F, 1F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.TRANSFER_GADGET.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsActiveProvider> activeProvider() {
        return EntityType.Builder.create(EntityLogisticsActiveProvider::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.LOGISTICS_FRAME_ACTIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsPassiveProvider> passiveProvider() {
        return EntityType.Builder.create(EntityLogisticsPassiveProvider::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.LOGISTICS_FRAME_PASSIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsStorage> storage() {
        return EntityType.Builder.create(EntityLogisticsStorage::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.LOGISTICS_FRAME_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsDefaultStorage> defaultStorage() {
        return EntityType.Builder.create(EntityLogisticsDefaultStorage::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.LOGISTICS_FRAME_DEFAULT_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsRequester> requester() {
        return EntityType.Builder.create(EntityLogisticsRequester::new, EntityClassification.MISC)
                .size(10 / 16F, 9 / 16F)
                .immuneToFire()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntities.LOGISTICS_FRAME_REQUESTER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }
}
