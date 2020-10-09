package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.projectile.EntityTumblingBlock;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetEntityMotion;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * Common code for the Air Cannon and Pneumatic Chestplate item launcher
 */
public class ItemLaunching {
    public static void launchEntity(Entity launchedEntity, Vector3d initialPos, Vector3d velocity, boolean doSpawn) {
        World world = launchedEntity.getEntityWorld();

        if (launchedEntity.getRidingEntity() != null) {
            launchedEntity.stopRiding();
        }

        launchedEntity.setPosition(initialPos.x, initialPos.y, initialPos.z);
        NetworkHandler.sendToAllAround(new PacketSetEntityMotion(launchedEntity, velocity),
                new PacketDistributor.TargetPoint(initialPos.x, initialPos.y, initialPos.z, 64, world.getDimensionKey()));
        if (launchedEntity instanceof FireballEntity) {
            // fireball velocity is handled a little differently...
            FireballEntity fireball = (FireballEntity) launchedEntity;
            fireball.accelerationX = velocity.x * 0.05;
            fireball.accelerationY = velocity.y * 0.05;
            fireball.accelerationZ = velocity.z * 0.05;
        } else {
            launchedEntity.setMotion(velocity);
        }
        launchedEntity.setOnGround(false);
//        launchedEntity.collided = false;
        launchedEntity.collidedHorizontally = false;
        launchedEntity.collidedVertically = false;

        if (doSpawn && !world.isRemote) {
            world.addEntity(launchedEntity);
        }

        for (int i = 0; i < 5; i++) {
            double velX = velocity.x * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            double velY = velocity.y * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            double velZ = velocity.z * 0.4D + (world.rand.nextGaussian() - 0.5D) * 0.05D;
            NetworkHandler.sendToAllAround(new PacketSpawnParticle(AirParticleData.DENSE, initialPos.x, initialPos.y, initialPos.z, velX, velY, velZ), world);
        }
        world.playSound(null, initialPos.x, initialPos.y, initialPos.z, ModSounds.AIR_CANNON.get(), SoundCategory.BLOCKS, 1f,world.rand.nextFloat() / 4f + 0.75f);
    }

    /**
     * Get the entity to launch for a given item.
     *
     * @param world the world
     * @param stack the item stack to be fired
     * @param dispenserLike true if dispenser-like behaviour should be used
     * @param fallingBlocks true if block items should be spawned as falling block entities rather than item entities
     * @return the entity to launch
     */
    public static Entity getEntityToLaunch(World world, ItemStack stack, PlayerEntity player, boolean dispenserLike, boolean fallingBlocks) {
        Item item = stack.getItem();
        if (dispenserLike) {
            if (item == Blocks.TNT.asItem()) {
                TNTEntity tnt = new TNTEntity(world, 0, 0, 0, player);
                tnt.setFuse(80);
                return tnt;
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                return new ExperienceBottleEntity(world, player);
            } else if (item instanceof PotionItem) {
                PotionEntity potionEntity = new PotionEntity(world, player);
                potionEntity.setItem(stack);
                return potionEntity;
            } else if (item instanceof ArrowItem) {
                return ((ArrowItem) item).createArrow(world, stack, player);
            } else if (item == Items.EGG) {
                return new EggEntity(world, player);
            } else if (item == Items.FIRE_CHARGE) {
                return new SmallFireballEntity(world, player, 0, 0, 0);
            } else if (item == Items.SNOWBALL) {
                return new SnowballEntity(world, player);
            } else if (item instanceof SpawnEggItem && world instanceof ServerWorld) {
                EntityType<?> type = ((SpawnEggItem) item).getType(stack.getTag());
                Entity e = type.spawn((ServerWorld) world, stack, player, player.getPosition(), SpawnReason.SPAWN_EGG, false, false);
                if (e instanceof LivingEntity && stack.hasDisplayName()) {
                    e.setCustomName(stack.getDisplayName());
                }
                return e;
            } else if (item instanceof MinecartItem) {
                return MinecartEntity.create(world, 0, 0, 0, ((MinecartItem) item).minecartType);
            }  else if (item instanceof BoatItem) {
                return new BoatEntity(world, 0, 0, 0);
            } else if (item == Items.FIREWORK_ROCKET) {
                return new FireworkRocketEntity(world, 0, 0, 0, stack);
            }
        }
        if (fallingBlocks && item instanceof BlockItem) {
            return new EntityTumblingBlock(world, player, 0, 0, 0, stack);
        } else {
            ItemEntity e = new ItemEntity(world, 0, 0, 0, stack);
            e.setPickupDelay(20);
            return e;
        }
    }
}
