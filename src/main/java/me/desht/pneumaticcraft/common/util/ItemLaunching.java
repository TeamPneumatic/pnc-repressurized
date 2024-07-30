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
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.projectile.MicromissileEntity;
import me.desht.pneumaticcraft.common.entity.projectile.TumblingBlockEntity;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetEntityMotion;
import me.desht.pneumaticcraft.common.network.PacketSpawnParticle;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.mixin.accessors.BoatItemAccess;
import me.desht.pneumaticcraft.mixin.accessors.MinecartItemAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Common code for the Air Cannon and Pneumatic Chestplate item launcher
 */
public class ItemLaunching {
    private static final List<ILaunchBehaviour> behaviours = new CopyOnWriteArrayList<>();

    public static void launchEntity(Entity launchedEntity, Vec3 initialPos, Vec3 velocity, boolean doSpawn) {
        Level world = launchedEntity.getCommandSenderWorld();

        if (launchedEntity.getVehicle() != null) {
            launchedEntity.stopRiding();
        }

        BlockPos trackPos = BlockPos.containing(initialPos);

        if (launchedEntity instanceof Boat) {
            // Boats are spawned slightly above the player to not push them down upon launching
            launchedEntity.setPos(initialPos.x, initialPos.y + 1, initialPos.z);
        } else {
            launchedEntity.setPos(initialPos.x, initialPos.y, initialPos.z);
        }

        NetworkHandler.sendToAllTracking(PacketSetEntityMotion.create(launchedEntity, velocity), world, trackPos);

        if (launchedEntity instanceof AbstractHurtingProjectile hurtingProjectile) {
            // fireball velocity is handled a little differently...
            hurtingProjectile.accelerationPower = 0.1;  // TODO needs testing
        }

        launchedEntity.setDeltaMovement(velocity);

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
            NetworkHandler.sendToAllTracking(PacketSpawnParticle.oneParticle(AirParticleData.DENSE,
                    initialPos.toVector3f(),
                    new Vec3(velX, velY, velZ).toVector3f()
            ), world, trackPos);
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
    public static Entity getEntityToLaunch(Level world, ItemStack stack, ServerPlayer player, boolean dispenserLike, boolean fallingBlocks) {
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
            ServerLevel level = (ServerLevel) player.level();  // player is a ServerPlayer, this is ok
            float playerYaw = player.getRotationVector().y;
            float playerPitch = player.getRotationVector().x;

            if (item == Items.ARMOR_STAND) {
                ArmorStand armorStand = new ArmorStand(level, 0, 0, 0);
                armorStand.setYRot(playerYaw);
                return armorStand;

            } else if (item instanceof ArrowItem arrowItem) {
                return arrowItem.createArrow(level, stack, player, null);

            } else if (item instanceof BoatItem boatItem) {
                HitResult dummyHitResult = new EntityHitResult(player, new Vec3(0, 0, 0));
                Boat boat = ((BoatItemAccess) boatItem).invokeGetBoat(level, dummyHitResult, stack, player);
                boat.setYRot(playerYaw);
                return boat;

            } else if (item == Items.EXPERIENCE_BOTTLE) {
                return new ThrownExperienceBottle(level, player);

            } else if (item instanceof PotionItem) {
                ThrownPotion potionEntity = new ThrownPotion(level, player);
                potionEntity.setItem(stack);
                return potionEntity;

            } else if (item == Blocks.TNT.asItem()) {
                PrimedTnt tnt = new PrimedTnt(level, 0, 0, 0, player);
                tnt.setFuse(80);
                return tnt;

            } else if (item == Items.EGG) {
                return new ThrownEgg(level, player);

            } else if (item == Items.FIRE_CHARGE) {
                SmallFireball e = new SmallFireball(level, player, Vec3.ZERO);
                e.setItem(stack);
                return e;

            } else if (item == Items.SNOWBALL) {
                return new Snowball(level, player);

            } else if (item instanceof SpawnEggItem egg) {
                EntityType<?> type = egg.getType(stack);
                Entity e = type.spawn(level, stack, player, player.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);

                if (e instanceof LivingEntity && stack.has(DataComponents.CUSTOM_NAME)) {
                    e.setCustomName(stack.getHoverName());
                }

                return e;

            } else if (item instanceof MinecartItem mi) {
                AbstractMinecart minecart = Minecart.createMinecart(level, 0, 0, 0, ((MinecartItemAccess) mi).getType(), stack, player);
                minecart.setYRot(playerYaw);
                return minecart;

            } else if (item == Items.FIREWORK_ROCKET) {
                return new FireworkRocketEntity(level, stack, 0, 0, 0, true);

            } else if (item == Items.TRIDENT) {
                stack.hurtAndBreak(1, (ServerLevel) player.level(), player, i -> { });
                return new ThrownTrident(level, player, stack);

            } else if (item == ModItems.MICROMISSILES.get()) {
                stack.hurtAndBreak(1, (ServerLevel) player.level(), player, i -> { });
                MicromissileEntity micromissile = new MicromissileEntity(level, player, stack);

                // Sets micromissile launch rotation
                micromissile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1/3f, 0.0F);

                // Puts micromissile on cooldown which is 1/3 of normal cooldown to reward launching
                player.getCooldowns().addCooldown(stack.getItem(), ConfigHelper.common().micromissiles.launchCooldown.get() / 3);

                return micromissile;
            }

            return null;
        });
    }
}
