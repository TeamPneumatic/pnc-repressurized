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

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.item.ILaunchBehaviour;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetEntityMotion;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Common code for the Air Cannon and Pneumatic Chestplate item launcher
 */
public class ItemLaunching {
    public static void launchEntity(Entity launchedEntity, Vec3 initialPos, Vec3 velocity, boolean doSpawn) {
        Level world = launchedEntity.getCommandSenderWorld();

        if (launchedEntity.getVehicle() != null) {
            launchedEntity.stopRiding();
        }

        BlockPos trackPos = new BlockPos(initialPos);
        launchedEntity.setPos(initialPos.x, initialPos.y, initialPos.z);
        NetworkHandler.sendToAllTracking(new PacketSetEntityMotion(launchedEntity, velocity), world, trackPos);
        if (launchedEntity instanceof Fireball fireball) {
            // fireball velocity is handled a little differently...
            fireball.xPower = velocity.x * 0.05;
            fireball.yPower = velocity.y * 0.05;
            fireball.zPower = velocity.z * 0.05;
        } else {
            launchedEntity.setDeltaMovement(velocity);
        }
        launchedEntity.setOnGround(false);
        launchedEntity.horizontalCollision = false;
        launchedEntity.verticalCollision = false;

        if (doSpawn && !world.isClientSide) {
            world.addFreshEntity(launchedEntity);
        }

        for (int i = 0; i < 5; i++) {
            double velX = velocity.x * 0.4D + (world.random.nextGaussian() - 0.5D) * 0.05D;
            double velY = velocity.y * 0.4D + (world.random.nextGaussian() - 0.5D) * 0.05D;
            double velZ = velocity.z * 0.4D + (world.random.nextGaussian() - 0.5D) * 0.05D;
            NetworkHandler.sendToAllTracking(new PacketSpawnParticle(AirParticleData.DENSE, initialPos.x, initialPos.y, initialPos.z, velX, velY, velZ), world, trackPos);
        }
        world.playSound(null, initialPos.x, initialPos.y, initialPos.z, ModSounds.AIR_CANNON.get(), SoundSource.BLOCKS, 1f,world.random.nextFloat() / 4f + 0.75f);
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
    public static Entity getEntityToLaunch(Level world, ItemStack stack, Player player, boolean dispenserLike, boolean fallingBlocks) {
        Item item = stack.getItem();

        // Checks if item has a special launch behavior, returning the corresponding entity to launch if so
        if (dispenserLike) {
            for (ILaunchBehaviour behaviour : behaviours) {
                Entity e = behaviour.getEntityToLaunch(stack, player);
                if (e != null) {
                    return e;
                }
            }
        }

        // Checks if item is a block that should be launched as a tumbling block
        if (fallingBlocks && item instanceof BlockItem) {
            return new TumblingBlockEntity(world, player, 0, 0, 0, stack);

        // Fallback to launch a generic item entity
        } else {
            ItemEntity e = new ItemEntity(world, 0, 0, 0, stack);
            e.setPickUpDelay(20);
            return e;
        }
    }

    public static void registerBehaviour(ILaunchBehaviour behaviour) {
        behaviours.add(behaviour);
    }

    public static void registerDefaultBehaviours() {
        registerBehaviour((stack, player) -> {
            Item item = stack.getItem();
            Level level = player.getLevel();
            float playerYaw = player.getRotationVector().y;

            if (item == Items.ARMOR_STAND) {
                ArmorStand armorStand = new ArmorStand(level, 0, 0, 0);
                armorStand.setYRot(playerYaw);
                return armorStand;

            } else if (item instanceof ArrowItem) {
                return ((ArrowItem) item).createArrow(level, stack, player);

            } else if (item instanceof BoatItem) {
                return new Boat(level, 0, 0, 0);


            // Experience bottle launched as entity
            } else if (item == Items.EXPERIENCE_BOTTLE) {
                return new ThrownExperienceBottle(level, player);


            } else if (item instanceof PotionItem) {
                ThrownPotion potionEntity = new ThrownPotion(world, player);
                potionEntity.setItem(stack);
                return potionEntity;

                // TNT blocks launch as entity
            } else if (item == Blocks.TNT.asItem()) {
                PrimedTnt tnt = new PrimedTnt(level, 0, 0, 0, player);
                tnt.setFuse(80);
                return tnt;

            } else if (item == Items.EGG) {
                return new ThrownEgg(level, player);

            } else if (item == Items.FIRE_CHARGE) {
                SmallFireball e = new SmallFireball(world, player, 0, 0, 0);
                e.setItem(stack);
                return e;

            } else if (item == Items.SNOWBALL) {
                return new Snowball(level, player);

            } else if (item instanceof SpawnEggItem && level instanceof ServerLevel) {
                EntityType<?> type = ((SpawnEggItem) item).getType(stack.getTag());
                Entity e = type.spawn((ServerLevel) level, stack, player, player.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);

                if (e instanceof LivingEntity && stack.hasCustomHoverName()) {
                    e.setCustomName(stack.getHoverName());
                }

                return e;

            } else if (item instanceof MinecartItem) {
                return Minecart.createMinecart(level, 0, 0, 0, ((MinecartItem) item).type);



            } else if (item == Items.FIREWORK_ROCKET) {
                return new FireworkRocketEntity(world, 0, 0, 0, stack);
            }
        }
        if (fallingBlocks && item instanceof BlockItem) {
            return new TumblingBlockEntity(world, player, 0, 0, 0, stack);
        } else {
            ItemEntity e = new ItemEntity(world, 0, 0, 0, stack);
            e.setPickUpDelay(20);
            return e;
        }
    }
}
