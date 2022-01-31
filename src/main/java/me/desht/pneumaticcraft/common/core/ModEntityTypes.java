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
import me.desht.pneumaticcraft.common.entity.EntityProgrammableController;
import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.*;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
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
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, Names.MOD_ID);

    public static final RegistryObject<EntityType<EntityDrone>> DRONE
            = register("drone", ModEntityTypes::drone);
    public static final RegistryObject<EntityType<EntityLogisticsDrone>> LOGISTICS_DRONE
            = register("logistics_drone", ModEntityTypes::logisticsDrone);
    public static final RegistryObject<EntityType<EntityHarvestingDrone>> HARVESTING_DRONE
            = register("harvesting_drone", ModEntityTypes::harvestingDrone);
    public static final RegistryObject<EntityType<EntityGuardDrone>> GUARD_DRONE
            = register("guard_drone", ModEntityTypes::guardDrone);
    public static final RegistryObject<EntityType<EntityCollectorDrone>> COLLECTOR_DRONE
            = register("collector_drone", ModEntityTypes::collectorDrone);

    public static final RegistryObject<EntityType<EntityAmadrone>> AMADRONE
            = register("amadrone", ModEntityTypes::amadrone);
    public static final RegistryObject<EntityType<EntityProgrammableController>> PROGRAMMABLE_CONTROLLER
            = register("programmable_controller", ModEntityTypes::programmableController);

    public static final RegistryObject<EntityType<EntityVortex>> VORTEX
            = register("vortex", ModEntityTypes::vortex);
    public static final RegistryObject<EntityType<EntityMicromissile>> MICROMISSILE
            = register("micromissile", ModEntityTypes::micromissile);
    public static final RegistryObject<EntityType<EntityTumblingBlock>> TUMBLING_BLOCK
            = register("tumbling_block", ModEntityTypes::tumblingBlock);
    public static final RegistryObject<EntityType<EntityRing>> RING
            = register("ring", ModEntityTypes::ring);

    public static final RegistryObject<EntityType<EntityCropSupport>> CROP_SUPPORT
            = register("crop_support", ModEntityTypes::cropSupport);
    public static final RegistryObject<EntityType<EntitySpawnerAgitator>> SPAWNER_AGITATOR
            = register("spawner_agitator", ModEntityTypes::spawnerAgitator);
    public static final RegistryObject<EntityType<EntityHeatFrame>> HEAT_FRAME
            = register("heat_frame", ModEntityTypes::heatFrame);
    public static final RegistryObject<EntityType<EntityTransferGadget>> TRANSFER_GADGET
            = register("transfer_gadget", ModEntityTypes::transferGadget);
    public static final RegistryObject<EntityType<EntityLogisticsActiveProvider>> LOGISTICS_FRAME_ACTIVE_PROVIDER
            = register("logistics_frame_active_provider", ModEntityTypes::activeProvider);
    public static final RegistryObject<EntityType<EntityLogisticsPassiveProvider>> LOGISTICS_FRAME_PASSIVE_PROVIDER
            = register("logistics_frame_passive_provider", ModEntityTypes::passiveProvider);
    public static final RegistryObject<EntityType<EntityLogisticsStorage>> LOGISTICS_FRAME_STORAGE
            = register("logistics_frame_storage", ModEntityTypes::storage);
    public static final RegistryObject<EntityType<EntityLogisticsDefaultStorage>> LOGISTICS_FRAME_DEFAULT_STORAGE
            = register("logistics_frame_default_storage", ModEntityTypes::defaultStorage);
    public static final RegistryObject<EntityType<EntityLogisticsRequester>> LOGISTICS_FRAME_REQUESTER
            = register("logistics_frame_requester", ModEntityTypes::requester);

    private static <E extends Entity> RegistryObject<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<EntityVortex> vortex() {
        return EntityType.Builder.of(EntityVortex::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(3)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.VORTEX.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityDrone> drone() {
        return EntityType.Builder.<EntityDrone>of(EntityDrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityAmadrone> amadrone() {
        return EntityType.Builder.of(EntityAmadrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.AMADRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityLogisticsDrone> logisticsDrone() {
        return EntityType.Builder.<EntityLogisticsDrone>of(EntityLogisticsDrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.LOGISTICS_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityHarvestingDrone> harvestingDrone() {
        return EntityType.Builder.<EntityHarvestingDrone>of(EntityHarvestingDrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.HARVESTING_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityGuardDrone> guardDrone() {
        return EntityType.Builder.<EntityGuardDrone>of(EntityGuardDrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.GUARD_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityCollectorDrone> collectorDrone() {
        return EntityType.Builder.<EntityCollectorDrone>of(EntityCollectorDrone::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.COLLECTOR_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityProgrammableController> programmableController() {
        return EntityType.Builder.of(EntityProgrammableController::new, MobCategory.CREATURE)
                .sized(0.35f, 0.175f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.PROGRAMMABLE_CONTROLLER.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityMicromissile> micromissile() {
        return EntityType.Builder.<EntityMicromissile>of(EntityMicromissile::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.MICROMISSILE.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityTumblingBlock> tumblingBlock() {
        return EntityType.Builder.<EntityTumblingBlock>of(EntityTumblingBlock::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.TUMBLING_BLOCK.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityRing> ring() {
        return EntityType.Builder.<EntityRing>of(EntityRing::new, MobCategory.MISC)
                .sized(0.5f, 0.5f)
                .fireImmune()
                .setTrackingRange(4)
                .setUpdateInterval(20)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.RING.get().create(world))
                .setShouldReceiveVelocityUpdates(true);
    }

    private static EntityType.Builder<EntityCropSupport> cropSupport() {
        return EntityType.Builder.of(EntityCropSupport::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.CROP_SUPPORT.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntitySpawnerAgitator> spawnerAgitator() {
        return EntityType.Builder.of(EntitySpawnerAgitator::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.SPAWNER_AGITATOR.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityHeatFrame> heatFrame() {
        return EntityType.Builder.of(EntityHeatFrame::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.HEAT_FRAME.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityTransferGadget> transferGadget() {
        return EntityType.Builder.of(EntityTransferGadget::new, MobCategory.MISC)
                .sized(1F, 1F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.TRANSFER_GADGET.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsActiveProvider> activeProvider() {
        return EntityType.Builder.of(EntityLogisticsActiveProvider::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_ACTIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsPassiveProvider> passiveProvider() {
        return EntityType.Builder.of(EntityLogisticsPassiveProvider::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsStorage> storage() {
        return EntityType.Builder.of(EntityLogisticsStorage::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsDefaultStorage> defaultStorage() {
        return EntityType.Builder.of(EntityLogisticsDefaultStorage::new, MobCategory.MISC)
                .sized(10 / 16F, 9 / 16F)
                .fireImmune()
                .setTrackingRange(3)
                .setUpdateInterval(Integer.MAX_VALUE)
                .setCustomClientFactory((spawnEntity, world) -> ModEntityTypes.LOGISTICS_FRAME_DEFAULT_STORAGE.get().create(world))
                .setShouldReceiveVelocityUpdates(false);
    }

    private static EntityType.Builder<EntityLogisticsRequester> requester() {
        return EntityType.Builder.of(EntityLogisticsRequester::new, MobCategory.MISC)
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
            event.put(ModEntityTypes.DRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.AMADRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.COLLECTOR_DRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.GUARD_DRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.HARVESTING_DRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.LOGISTICS_DRONE.get(), EntityDrone.prepareAttributes().build());
            event.put(ModEntityTypes.PROGRAMMABLE_CONTROLLER.get(), EntityDrone.prepareAttributes().build());
        }
    }
}
