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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.entity.RingEntity;
import me.desht.pneumaticcraft.common.entity.drone.*;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import me.desht.pneumaticcraft.common.entity.projectile.VortexEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Names.MOD_ID);

    public static final Supplier<EntityType<DroneEntity>> DRONE
            = register("drone", ModEntityTypes::drone);
    public static final Supplier<EntityType<LogisticsDroneEntity>> LOGISTICS_DRONE
            = register("logistics_drone", ModEntityTypes::logisticsDrone);
    public static final Supplier<EntityType<HarvestingDroneEntity>> HARVESTING_DRONE
            = register("harvesting_drone", ModEntityTypes::harvestingDrone);
    public static final Supplier<EntityType<GuardDroneEntity>> GUARD_DRONE
            = register("guard_drone", ModEntityTypes::guardDrone);
    public static final Supplier<EntityType<CollectorDroneEntity>> COLLECTOR_DRONE
            = register("collector_drone", ModEntityTypes::collectorDrone);

    public static final Supplier<EntityType<AmadroneEntity>> AMADRONE
            = register("amadrone", ModEntityTypes::amadrone);
    public static final Supplier<EntityType<ProgrammableControllerEntity>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ModEntityTypes::programmableController);

    public static final Supplier<EntityType<VortexEntity>> VORTEX
            = register("vortex", ModEntityTypes::vortex);
    public static final Supplier<EntityType<MicromissileEntity>> MICROMISSILE
            = register("micromissile", ModEntityTypes::micromissile);
    public static final Supplier<EntityType<TumblingBlockEntity>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntityTypes::tumblingBlock);
    public static final Supplier<EntityType<RingEntity>> RING
            = register("ring", ModEntityTypes::ring);

    public static final Supplier<EntityType<CropSupportEntity>> CROP_SUPPORT
            = register("crop_support", ModEntityTypes::cropSupport);
    public static final Supplier<EntityType<SpawnerAgitatorEntity>> SPAWNER_AGITATOR
            = register("spawner_agitator", ModEntityTypes::spawnerAgitator);
    public static final Supplier<EntityType<HeatFrameEntity>> HEAT_FRAME
            = register("heat_frame", ModEntityTypes::heatFrame);
    public static final Supplier<EntityType<TransferGadgetEntity>> TRANSFER_GADGET
            = register("transfer_gadget", ModEntityTypes::transferGadget);
    public static final Supplier<EntityType<LogisticsActiveProviderEntity>> LOGISTICS_FRAME_ACTIVE_PROVIDER
            = register("logistics_frame_active_provider", ModEntityTypes::activeProvider);
    public static final Supplier<EntityType<LogisticsPassiveProviderEntity>> LOGISTICS_FRAME_PASSIVE_PROVIDER
            = register("logistics_frame_passive_provider", ModEntityTypes::passiveProvider);
    public static final Supplier<EntityType<LogisticsStorageEntity>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ModEntityTypes::storage);
    public static final Supplier<EntityType<LogisticsDefaultStorageEntity>> LOGISTICS_FRAME_DEFAULT_STORAGE
            = register("logistics_frame_default_storage", ModEntityTypes::defaultStorage);
    public static final Supplier<EntityType<LogisticsRequesterEntity>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ModEntityTypes::requester);

    private static <E extends Entity> Supplier<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<VortexEntity> vortex() {
        return EntityType.Builder.of(VortexEntity::new, MobCategory.MISC)
                .sized(1.5f, 1.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<DroneEntity> drone() {
        return EntityType.Builder.<DroneEntity>of(DroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<AmadroneEntity> amadrone() {
        return EntityType.Builder.of(AmadroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<LogisticsDroneEntity> logisticsDrone() {
        return EntityType.Builder.<LogisticsDroneEntity>of(LogisticsDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<HarvestingDroneEntity> harvestingDrone() {
        return EntityType.Builder.<HarvestingDroneEntity>of(HarvestingDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<GuardDroneEntity> guardDrone() {
        return EntityType.Builder.<GuardDroneEntity>of(GuardDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<CollectorDroneEntity> collectorDrone() {
        return EntityType.Builder.<CollectorDroneEntity>of(CollectorDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<ProgrammableControllerEntity> programmableController() {
        return EntityType.Builder.of(ProgrammableControllerEntity::new, MobCategory.CREATURE)
                .sized(0.35f, 0.175f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<MicromissileEntity> micromissile() {
        return EntityType.Builder.<MicromissileEntity>of(MicromissileEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<TumblingBlockEntity> tumblingBlock() {
        return EntityType.Builder.<TumblingBlockEntity>of(TumblingBlockEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<RingEntity> ring() {
        return EntityType.Builder.<RingEntity>of(RingEntity::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<CropSupportEntity> cropSupport() {
        return EntityType.Builder.of(CropSupportEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<SpawnerAgitatorEntity> spawnerAgitator() {
        return EntityType.Builder.of(SpawnerAgitatorEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<HeatFrameEntity> heatFrame() {
        return EntityType.Builder.of(HeatFrameEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<TransferGadgetEntity> transferGadget() {
        return EntityType.Builder.of(TransferGadgetEntity::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsActiveProviderEntity> activeProvider() {
        return EntityType.Builder.of(LogisticsActiveProviderEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsPassiveProviderEntity> passiveProvider() {
        return EntityType.Builder.of(LogisticsPassiveProviderEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsStorageEntity> storage() {
        return EntityType.Builder.of(LogisticsStorageEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsDefaultStorageEntity> defaultStorage() {
        return EntityType.Builder.of(LogisticsDefaultStorageEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<LogisticsRequesterEntity> requester() {
        return EntityType.Builder.of(LogisticsRequesterEntity::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setShouldReceiveVelocityUpdates(false);
    }

    @EventBusSubscriber(modid = Names.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
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
