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

package me.desht.pneumaticcraft.common.minigun;

import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.MovingSoundFocus;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class Minigun {
    public static final float MAX_GUN_SPEED = 0.4f;
    private static final float MAX_GUN_YAW_CHANGE = 10f;
    private static final float MAX_GUN_PITCH_CHANGE = 10f;

    private final boolean requiresTarget;

    private float minigunSpeed;
    private int minigunTriggerTimeOut;
    //    private int minigunSoundCounter = -1;
    private final RandomSource rand = RandomSource.createNewThreadLocalInstance();
    private float minigunRotation, oldMinigunRotation;
    public float minigunYaw, oldMinigunYaw;
    public float minigunPitch, oldMinigunPitch;
    private boolean sweeping; // When true, the yaw of the minigun will sweep with a sinus pattern when not targeting.
    private boolean returning; // When true, the minigun is returning to its idle position.
    private float sweepingProgress;

    private boolean gunAimedAtTarget;

    private IAirHandler airCapability = null;
    private int airUsage;
    private ItemStack ammoStack = ItemStack.EMPTY;
    protected final Player player;
    protected Level world;
    private LivingEntity attackTarget;
    private float idleYaw;
    private boolean infiniteAmmo = false;

    public Minigun(Player player, boolean requiresTarget) {
        this.player = player;
        this.requiresTarget = requiresTarget;
    }

    public Minigun setAirHandler(IAirHandler airHandler, int airUsage) {
        this.airCapability = airHandler;
        this.airUsage = airUsage;
        return this;
    }

    public Minigun setAmmoStack(@Nonnull ItemStack ammoStack) {
        this.ammoStack = ammoStack;
        return this;
    }

    @Nonnull
    public ItemStack getAmmoStack() {
        return ammoStack;
    }

    public Player getPlayer() {
        return player;
    }

    public Minigun setWorld(Level world) {
        this.world = world;
        return this;
    }

    public Level getWorld() {
        return world;
    }

    public Minigun setAttackTarget(LivingEntity entity) {
        attackTarget = entity;
        return this;
    }

    public boolean isValid() {
        return true;
    }

    public abstract boolean isMinigunActivated();

    public abstract void setMinigunActivated(boolean activated);

    public abstract void setAmmoColorStack(@Nonnull ItemStack ammo);

    public abstract int getAmmoColor();

    public abstract void playSound(SoundEvent soundName, float volume, float pitch);

    /**
     * The position from which particles originate when the gun is firing
     * @return a vector position, may be null
     */
    @Nullable
    public abstract Vec3 getMuzzlePosition();

    /**
     * A normalised look vector for the minigun
     * @return a vector
     */
    public abstract Vec3 getLookAngle();

    public abstract float getParticleScale();

    protected int getAmmoColor(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof AbstractGunAmmoItem a ? a.getAmmoColor(stack) : 0xFF313131;
    }

    public Optional<? extends IAirHandler> getAirCapability() {
        return Optional.ofNullable(airCapability);
    }

    /**
     * Get the source for this sound, where the client should play the sound loop at.  Can be an Entity, a
     * TileEntity, or a BlockPos; anything else will cause an exception to be thrown.
     *
     * @return the sound's source
     */
    public MovingSoundFocus getSoundSource() {
        return MovingSoundFocus.of(player);
    }

    public float getMinigunSpeed() {
        return minigunSpeed;
    }

    public void setMinigunSpeed(float minigunSpeed) {
        this.minigunSpeed = minigunSpeed;
    }

    public int getMinigunTriggerTimeOut() {
        return minigunTriggerTimeOut;
    }

    public void setMinigunTriggerTimeOut(int minigunTriggerTimeOut) {
        this.minigunTriggerTimeOut = minigunTriggerTimeOut;
    }

    public float getMinigunRotation() {
        return minigunRotation;
    }

    public void setMinigunRotation(float minigunRotation) {
        this.minigunRotation = minigunRotation;
    }

    public float getOldMinigunRotation() {
        return oldMinigunRotation;
    }

    public void setOldMinigunRotation(float oldMinigunRotation) {
        this.oldMinigunRotation = oldMinigunRotation;
    }

    public LivingEntity getAttackTarget() {
        return attackTarget;
    }

    public void setSweeping(boolean sweeping) {
        this.sweeping = sweeping;
    }

    public boolean isSweeping() {
        return sweeping;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public void setIdleYaw(float idleYaw) {
        this.idleYaw = clampYaw(idleYaw);
    }

    public boolean isGunAimedAtTarget() {
        return !requiresTarget || gunAimedAtTarget;
    }

    public boolean isInfiniteAmmo() {
        return infiniteAmmo;
    }

    public Minigun setInfiniteAmmo(boolean infiniteAmmo) {
        this.infiniteAmmo = infiniteAmmo;
        return  this;
    }

    public boolean tryFireMinigun(Entity target) {
        boolean lastShotOfAmmo = false;
        if (!ammoStack.isEmpty() && ammoStack.getDamageValue() < ammoStack.getMaxDamage() && airCapability.getPressure() > 0) {
            setMinigunTriggerTimeOut(10);
            if (!world.isClientSide && getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget)) {
                HitResult rtr = null;
                AbstractGunAmmoItem ammoItem = (AbstractGunAmmoItem) ammoStack.getItem();
                if (!requiresTarget) {
                    rtr = RayTraceUtils.getMouseOverServer(player, getRange());
                    target = rtr instanceof EntityHitResult e ? e.getEntity() : null;
                }
                if (airCapability != null) {
                    int usage = (int) Math.ceil(airUsage * ammoItem.getAirUsageMultiplier(this, ammoStack));
                    usage += getUpgrades(ModUpgrades.RANGE.get());
                    if (getUpgrades(ModUpgrades.SPEED.get()) > 0) {
                        usage *= getUpgrades(ModUpgrades.SPEED.get()) + 1;
                    }
                    if (getPlayer() != null && !getPlayer().isCreative()) {
                        airCapability.addAir(-usage);
                    }
                }
                int roundsUsed = 1;
                if (target != null) {
                    if (getUpgrades(ModUpgrades.SECURITY.get()) == 0 || !securityProtectedTarget(target)) {
                        roundsUsed = ammoItem.onTargetHit(this, ammoStack, target);
                    }
                } else if (rtr != null && rtr.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult brtr = (BlockHitResult) rtr;
                    roundsUsed = ammoItem.onBlockHit(this, ammoStack, brtr);
                }
                int ammoCost = roundsUsed * ammoItem.getAmmoCost(ammoStack);
                if (!isInfiniteAmmo()) {
                    lastShotOfAmmo = ammoStack.hurt(ammoCost, rand, player instanceof ServerPlayer ? (ServerPlayer) player : null)
                            && ammoStack.getEnchantmentLevel(Enchantments.UNBREAKING) == 0;
                }
            }
        }
        return lastShotOfAmmo;
    }

    private boolean securityProtectedTarget(Entity target) {
        if (target instanceof TamableAnimal) {
            return ((TamableAnimal) target).getOwner() != null;
        } else if (target instanceof DroneEntity) {
            return ((DroneEntity) target).getOwner().getUUID().equals(getPlayer().getUUID());
        } else {
            return target instanceof Player;
        }
    }

    public void tick(double posX, double posY, double posZ) {
        setOldMinigunRotation(getMinigunRotation());
        oldMinigunYaw = minigunYaw;
        oldMinigunPitch = minigunPitch;

        if (attackTarget != null && !attackTarget.isAlive()) attackTarget = null;

        setMinigunActivated(getMinigunTriggerTimeOut() > 0);

        setAmmoColorStack(ammoStack);

        if (isMinigunActivated()) {
            // spin up
            setMinigunTriggerTimeOut(getMinigunTriggerTimeOut() - 1);
            if (getMinigunSpeed() == 0) {
                playSound(ModSounds.HUD_INIT.get(), 3, 0.9F);
            }
            float speedBonus = getUpgrades(ModUpgrades.SPEED.get()) * 0.0033F;
            float lastSpeed = getMinigunSpeed();
            setMinigunSpeed(Math.min(getMinigunSpeed() + 0.01F + speedBonus, MAX_GUN_SPEED));
            if (!world.isClientSide && getMinigunSpeed() > lastSpeed && getMinigunSpeed() >= MAX_GUN_SPEED) {
                // reached max speed: start playing the looping sound
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.MINIGUN, getSoundSource()), player.level(), BlockPos.containing(posX, posY, posZ));
            }
        } else {
            // spin down
            setMinigunSpeed(Math.max(0F, getMinigunSpeed() - 0.003F));
        }

        setMinigunRotation(getMinigunRotation() + getMinigunSpeed());

        if (attackTarget != null) {
            // swing toward the target entity
            double deltaX = posX - attackTarget.getX();
            double deltaY = posY - (attackTarget.getY() + attackTarget.getBbHeight() / 2);
            double deltaZ = posZ - attackTarget.getZ();

            float targetYaw = (float) clampYaw(180 - Math.toDegrees(Math.atan2(deltaX, deltaZ)));
            float targetPitch = (float) Math.toDegrees(Math.atan(deltaY / PneumaticCraftUtils.distBetween(0, 0, deltaX, deltaZ)));//posX, posZ, attackTarget.getPosX(), attackTarget.getPosZ())));

            minigunYaw = minigunPitch < -80 || minigunPitch > 80 ? targetYaw : moveToward(minigunYaw, targetYaw, MAX_GUN_YAW_CHANGE, true);
            minigunPitch = moveToward(minigunPitch, targetPitch, MAX_GUN_PITCH_CHANGE, false);
            gunAimedAtTarget = PneumaticCraftUtils.epsilonEquals(minigunYaw, targetYaw) && PneumaticCraftUtils.epsilonEquals(minigunPitch, targetPitch);
        } else if (isReturning()) {
            // sentry turret returning to idle position
            minigunYaw = moveToward(minigunYaw, idleYaw, MAX_GUN_YAW_CHANGE, true);
            minigunPitch = moveToward(minigunPitch, 0F, MAX_GUN_PITCH_CHANGE, false);
            if (PneumaticCraftUtils.epsilonEquals(minigunYaw, idleYaw)) {
                setReturning(false);
            }
            sweepingProgress = 0F;
        } else if (isSweeping()) {
            // sentry turret idly sweeping left to right
            minigunYaw = clampYaw(idleYaw + Mth.sin(sweepingProgress) * 22);
            minigunPitch = moveToward(minigunPitch, 0F, MAX_GUN_PITCH_CHANGE, false);
            sweepingProgress += 0.05F;
        }
    }

    private float moveToward(float val, float target, float amount, boolean yaw) {
        if (yaw && Math.abs(val - target) > 180) amount = -amount;
        if (val > target) {
            val = Math.max(val - amount, target);
        } else {
            val = Math.min(val + amount, target);
        }
        return yaw ? clampYaw(val) : val;
    }

    public int getUpgrades(PNCUpgrade upgrade) {
        return 0;
    }

    public double getRange() {
        double mul = getAmmoStack().getItem() instanceof AbstractGunAmmoItem a ? a.getRangeMultiplier(ammoStack) : 1;
        return (ConfigHelper.common().minigun.baseRange.get() + 5 * getUpgrades(ModUpgrades.RANGE.get())) * mul;
    }

    public boolean dispenserWeightedPercentage(int basePct) {
        return dispenserWeightedPercentage(basePct, 0.1f);
    }

    public boolean dispenserWeightedPercentage(int basePct, float dispenserWeight) {
        return getWorld().random.nextInt(100) < basePct * (1 + getUpgrades(ModUpgrades.DISPENSER.get()) * dispenserWeight);
    }

    public static float clampYaw(float yaw) {
        while (yaw > 180F) yaw -= 360F;
        while (yaw < -180F) yaw += 360F;
        return yaw;
    }

    public static double clampYaw(double yaw) {
        while (yaw > 180D) yaw -= 360D;
        while (yaw < -180D) yaw += 360D;
        return yaw;
    }
}
