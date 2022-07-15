/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.entity.RingEntity;
import me.desht.pneumaticcraft.common.entity.drone.*;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import me.desht.pneumaticcraft.common.entity.projectile.VortexEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Names.MOD_ID);

    public static final RegistryObject<EntityType<DroneEntity>> DRONE
            = register("drone", ModEntityTypes::drone);
    public static final RegistryObject<EntityType<LogisticsDroneEntity>> LOGISTICS_DRONE
            = register("logistics_drone", ModEntityTypes::logisticsDrone);
    public static final RegistryObject<EntityType<HarvestingDroneEntity>> HARVESTING_DRONE
            = register("harvesting_drone", ModEntityTypes::harvestingDrone);
    public static final RegistryObject<EntityType<GuardDroneEntity>> GUARD_DRONE
            = register("guard_drone", ModEntityTypes::guardDrone);
    public static final RegistryObject<EntityType<CollectorDroneEntity>> COLLECTOR_DRONE
            = register("collector_drone", ModEntityTypes::collectorDrone);

    public static final RegistryObject<EntityType<AmadroneEntity>> AMADRONE
            = register("amadrone", ModEntityTypes::amadrone);
    public static final RegistryObject<EntityType<ProgrammableControllerEntity>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ModEntityTypes::programmableController);

    public static final RegistryObject<EntityType<VortexEntity>> VORTEX
            = register("vortex", ModEntityTypes::vortex);
    public static final RegistryObject<EntityType<MicromissileEntity>> MICROMISSILE
            = register("micromissile", ModEntityTypes::micromissile);
    public static final RegistryObject<EntityType<TumblingBlockEntity>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntityTypes::tumblingBlock);
    public static final RegistryObject<EntityType<RingEntity>> RING
            = register("ring", ModEntityTypes::ring);

    public static final RegistryObject<EntityType<CropSupportEntity>> CROP_SUPPORT
            = register("crop_support", ModEntityTypes::cropSupport);
    public static final RegistryObject<EntityType<SpawnerAgitatorEntity>> SPAWNER_AGITATOR
            = register("spawner_agitator", ModEntityTypes::spawnerAgitator);
    public static final RegistryObject<EntityType<HeatFrameEntity>> HEAT_FRAME
            = register("heat_frame", ModEntityTypes::heatFrame);
    public static final RegistryObject<EntityType<TransferGadgetEntity>> TRANSFER_GADGET
            = register("transfer_gadget", ModEntityTypes::transferGadget);
    public static final RegistryObject<EntityType<LogisticsActiveProviderEntity>> LOGISTICS_FRAME_ACTIVE_PROVIDER
            = register("logistics_frame_active_provider", ModEntityTypes::activeProvider);
    public static final RegistryObject<EntityType<LogisticsPassiveProviderEntity>> LOGISTICS_FRAME_PASSIVE_PROVIDER
            = register("logistics_frame_passive_provider", ModEntityTypes::passiveProvider);
    public static final RegistryObject<EntityType<LogisticsStorageEntity>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ModEntityTypes::storage);
    public static final RegistryObject<EntityType<LogisticsDefaultStorageEntity>> LOGISTICS_FRAME_DEFAULT_STORAGE
            = register("logistics_frame_default_storage", ModEntityTypes::defaultStorage);
    public static final RegistryObject<EntityType<LogisticsRequesterEntity>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ModEntityTypes::requester);

    private static <E extends Entity> RegistryObject<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<VortexEntity> vortex() {
        return EntityType.Builder.of(VortexEntity::new, MobCategory.MISC)
                .sized(1.5f, 1.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(3)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.VORTEX.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<DroneEntity> drone() {
        return EntityType.Builder.<DroneEntity>of(DroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<AmadroneEntity> amadrone() {
        return EntityType.Builder.of(AmadroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.AMADRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<LogisticsDroneEntity> logisticsDrone() {
        return EntityType.Builder.<LogisticsDroneEntity>of(LogisticsDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.LOGISTICS_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<HarvestingDroneEntity> harvestingDrone() {
        return EntityType.Builder.<HarvestingDroneEntity>of(HarvestingDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.HARVESTING_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<GuardDroneEntity> guardDrone() {
        return EntityType.Builder.<GuardDroneEntity>of(GuardDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.GUARD_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<CollectorDroneEntity> collectorDrone() {
        return EntityType.Builder.<CollectorDroneEntity>of(CollectorDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.COLLECTOR_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<ProgrammableControllerEntity> programmableController() {
        return EntityType.Builder.of(ProgrammableControllerEntity::new, MobCategory.CREATURE)
                .sized(0.35f, 0.175f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.PROGRAMMABLE_CONTROLLER.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<MicromissileEntity> micromissile() {
        return EntityType.Builder.<MicromissileEntity>of(MicromissileEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.MICROMISSILE.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<TumblingBlockEntity> tumblingBlock() {
        return EntityType.Builder.<TumblingBlockEntity>of(TumblingBlockEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.TUMBLING_BLOCK.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<RingEntity> ring() {
        return EntityType.Builder.<RingEntity>of(RingEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.RING.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<CropSupportEntity> cropSupport() {
        return EntityType.Builder.of(CropSupportEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.CROP_SUPPORT.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<SpawnerAgitatorEntity> spawnerAgitator() {
        return EntityType.Builder.of(SpawnerAgitatorEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.SPAWNER_AGITATOR.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<HeatFrameEntity> heatFrame() {
        return EntityType.Builder.of(HeatFrameEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.HEAT_FRAME.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<TransferGadgetEntity> transferGadget() {
        return EntityType.Builder.of(TransferGadgetEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.TRANSFER_GADGET.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsActiveProviderEntity> activeProvider() {
        return EntityType.Builder.of(LogisticsActiveProviderEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_ACTIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsPassiveProviderEntity> passiveProvider() {
        return EntityType.Builder.of(LogisticsPassiveProviderEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsStorageEntity> storage() {
        return EntityType.Builder.of(LogisticsStorageEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsDefaultStorageEntity> defaultStorage() {
        return EntityType.Builder.of(LogisticsDefaultStorageEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_DEFAULT_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsRequesterEntity> requester() {
        return EntityType.Builder.of(LogisticsRequesterEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_REQUESTER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Listener {
        @SubscribeEvent
        public static void registerGlobalAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.DRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.AMADRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.COLLECTOR_DRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.GUARD_DRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.HARVESTING_DRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.LOGISTICS_DRONE.get(), DroneEntity.prepareAttributes().build());
            event.put(ModEntityTypes.PROGRAMMABLE_CONTROLLER.get(), DroneEntity.prepareAttributes().build());
        }
    }
}
