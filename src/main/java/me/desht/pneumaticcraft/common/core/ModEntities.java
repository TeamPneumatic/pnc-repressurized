package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.entity.EntityRing;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityHarvestingDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityLogisticsDrone;
import me.desht.pneumaticcraft.common.entity.projectile.EntityMicromissile;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.entity.projectile.EntityVortex;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Names.MOD_ID)
public class ModEntities {
    public static final EntityType<EntityVortex> VORTEX = null;
    public static final EntityType<EntityDrone> DRONE = null;
    public static final EntityType<EntityLogisticsDrone> LOGISTIC_DRONE = null;
    public static final EntityType<EntityHarvestingDrone> HARVESTING_DRONE = null;
    public static final EntityType<EntityMicromissile> MICROMISSILE = null;
    public static final EntityType<EntityTumblingBlock> TUMBLING_BLOCK = null;
    public static final EntityType<EntityRing> RING = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void onEntityRegister(RegistryEvent.Register<EntityType<?>> event) {
            IForgeRegistry<EntityType<?>> r = event.getRegistry();

            r.register(EntityType.Builder.create(EntityVortex::create, EntityClassification.MISC)
                    .size(0.5f, 0.5f)
                    .immuneToFire()
                    .setTrackingRange(16)
                    .setUpdateInterval(3)
                    .setCustomClientFactory((spawnEntity, world) -> ModEntities.VORTEX.create(world))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":vortex")
                    .setRegistryName("vortex"));

            r.register(EntityType.Builder.create(EntityDrone::create, EntityClassification.CREATURE)
                    .size(0.7f, 0.35f)
                    .setTrackingRange(32)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(((spawnEntity, world) -> ModEntities.DRONE.create(world)))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":drone")
                    .setRegistryName("drone"));

            r.register(EntityType.Builder.create(EntityLogisticsDrone::create, EntityClassification.CREATURE)
                    .size(0.7f, 0.35f)
                    .setTrackingRange(32)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(((spawnEntity, world) -> ModEntities.LOGISTIC_DRONE.create(world)))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":logistic_drone")
                    .setRegistryName("logistic_drone"));

            r.register(EntityType.Builder.create(EntityHarvestingDrone::create, EntityClassification.CREATURE)
                    .size(0.7f, 0.35f)
                    .setTrackingRange(32)
                    .setUpdateInterval(3)
                    .setCustomClientFactory(((spawnEntity, world) -> ModEntities.HARVESTING_DRONE.create(world)))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":harvesting_drone")
                    .setRegistryName("harvesting_drone"));

            r.register(EntityType.Builder.create(EntityMicromissile::create, EntityClassification.MISC)
                    .size(0.5f, 0.5f)
                    .immuneToFire()
                    .setTrackingRange(4)
                    .setUpdateInterval(20)
                    .setCustomClientFactory((spawnEntity, world) -> ModEntities.MICROMISSILE.create(world))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":micromissile")
                    .setRegistryName("micromissile"));

            r.register(EntityType.Builder.create(EntityTumblingBlock::create, EntityClassification.MISC)
                    .size(0.98f, 0.98f)
                    .immuneToFire()
                    .setTrackingRange(4)
                    .setUpdateInterval(20)
                    .setCustomClientFactory((spawnEntity, world) -> ModEntities.TUMBLING_BLOCK.create(world))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":tumbling_block")
                    .setRegistryName("tumbling_block"));

            r.register(EntityType.Builder.create(EntityRing::create, EntityClassification.MISC)
                    .size(0.5f, 0.5f)
                    .immuneToFire()
                    .setTrackingRange(4)
                    .setUpdateInterval(20)
                    .setCustomClientFactory((spawnEntity, world) -> ModEntities.RING.create(world))
                    .setShouldReceiveVelocityUpdates(true)
                    .build(Names.MOD_ID + ":ring")
                    .setRegistryName("ring"));
        }

    }
}
