package me.desht.pneumaticcraft.common.minigun;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.sound.MovingSounds;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound;
import me.desht.pneumaticcraft.common.network.PacketPlayMovingSound.SoundSource;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.RayTraceUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Minigun {
    public static final float MAX_GUN_SPEED = 0.4f;
    private static final float MAX_GUN_YAW_CHANGE = 10f;
    private static final float MAX_GUN_PITCH_CHANGE = 10f;

    private final boolean requiresTarget;

    private float minigunSpeed;
    private int minigunTriggerTimeOut;
    //    private int minigunSoundCounter = -1;
    private final Random rand = ThreadLocalRandom.current();
    private float minigunRotation, oldMinigunRotation;
    public float minigunYaw, oldMinigunYaw;
    public float minigunPitch, oldMinigunPitch;
    private boolean sweeping; // When true, the yaw of the minigun will sweep with a sinus pattern when not targeting.
    private boolean returning; // When true, the minigun is returning to its idle position.
    private float sweepingProgress;

    private boolean gunAimedAtTarget;

    private LazyOptional<? extends IAirHandler> airCapability = LazyOptional.empty();
    private int airUsage;
    private ItemStack ammoStack = ItemStack.EMPTY;
    protected final PlayerEntity player;
    protected World world;
    private LivingEntity attackTarget;
    private float idleYaw;

    public Minigun(PlayerEntity player, boolean requiresTarget) {
        this.player = player;
        this.requiresTarget = requiresTarget;
    }

    public Minigun setAirHandler(LazyOptional<? extends IAirHandler> airHandler, int airUsage) {
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

    public PlayerEntity getPlayer() {
        return player;
    }

    public Minigun setWorld(World world) {
        this.world = world;
        return this;
    }

    public World getWorld() {
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

    protected int getAmmoColor(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemGunAmmo ?
                ((ItemGunAmmo) stack.getItem()).getAmmoColor(stack) :
                0xFF313131;
    }

    public LazyOptional<? extends IAirHandler> getAirCapability() {
        return airCapability;
    }

    /**
     * Get the source for this sound, where the client should play the sound loop at.  Can be an Entity, a
     * TileEntity, or a BlockPos; anything else will cause an exception to be thrown.
     *
     * @return the sound's source
     */
    public SoundSource getSoundSource() {
        return SoundSource.of(player);
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

    public boolean tryFireMinigun(Entity target) {
        boolean lastShotOfAmmo = false;
        if (!ammoStack.isEmpty() && ammoStack.getDamage() < ammoStack.getMaxDamage() && airCapability.map(h -> h.getPressure() > 0).orElse(true)) {
            setMinigunTriggerTimeOut(10);
            if (!world.isRemote && getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget)) {
                RayTraceResult rtr = null;
                ItemGunAmmo ammoItem = (ItemGunAmmo) ammoStack.getItem();
                if (!requiresTarget) {
                    rtr = RayTraceUtils.getMouseOverServer(player, getRange());
                    target = rtr instanceof EntityRayTraceResult ? ((EntityRayTraceResult) rtr).getEntity() : null;
                }
                airCapability.ifPresent(airHandler -> {
                    int usage = (int) Math.ceil(airUsage * ammoItem.getAirUsageMultiplier(this, ammoStack));
                    usage += getUpgrades(EnumUpgrade.RANGE);
                    if (getUpgrades(EnumUpgrade.SPEED) > 0) {
                        usage *= getUpgrades(EnumUpgrade.SPEED) + 1;
                    }
                    airHandler.addAir(-usage);
                });
                int roundsUsed = 1;
                if (target != null) {
                    if (getUpgrades(EnumUpgrade.SECURITY) == 0 || !securityProtectedTarget(target)) {
                        roundsUsed = ammoItem.onTargetHit(this, ammoStack, target);
                    }
                } else if (rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
                    BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
                    roundsUsed = ammoItem.onBlockHit(this, ammoStack, brtr);
                }
                int ammoCost = roundsUsed * ammoItem.getAmmoCost(ammoStack);
                lastShotOfAmmo = ammoStack.attemptDamageItem(ammoCost, rand, player instanceof ServerPlayerEntity ? (ServerPlayerEntity) player : null);
            }
        }
        return lastShotOfAmmo;
    }

    private boolean securityProtectedTarget(Entity target) {
        if (target instanceof TameableEntity) {
            return ((TameableEntity) target).getOwner() != null;
        } else if (target instanceof EntityDrone) {
            return ((EntityDrone) target).getOwner().getUniqueID().equals(getPlayer().getUniqueID());
        } else {
            return target instanceof PlayerEntity;
        }
    }

    public void update(double posX, double posY, double posZ) {
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
            float speedBonus = getUpgrades(EnumUpgrade.SPEED) * 0.0033F;
            float lastSpeed = getMinigunSpeed();
            setMinigunSpeed(Math.min(getMinigunSpeed() + 0.01F + speedBonus, MAX_GUN_SPEED));
            if (!world.isRemote && getMinigunSpeed() > lastSpeed && getMinigunSpeed() >= MAX_GUN_SPEED) {
                // reached max speed: start playing the looping sound
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.MINIGUN, getSoundSource()), player.world, new BlockPos(posX, posY, posZ));
            }
        } else {
            // spin down
            setMinigunSpeed(Math.max(0F, getMinigunSpeed() - 0.003F));
        }

        setMinigunRotation(getMinigunRotation() + getMinigunSpeed());

        if (attackTarget != null) {
            // swing toward the target entity
            double deltaX = posX - attackTarget.getPosX();
            double deltaY = posY - (attackTarget.getPosY() + attackTarget.getHeight() / 2);
            double deltaZ = posZ - attackTarget.getPosZ();

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
            minigunYaw = clampYaw(idleYaw + MathHelper.sin(sweepingProgress) * 22);
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

    public int getUpgrades(EnumUpgrade upgrade) {
        return 0;
    }

    public double getRange() {
        double mul = getAmmoStack().getItem() instanceof ItemGunAmmo ? ((ItemGunAmmo) ammoStack.getItem()).getRangeMultiplier(ammoStack) : 1;
        return (PNCConfig.Common.Minigun.baseRange + 5 * getUpgrades(EnumUpgrade.RANGE)) * mul;
    }

    public boolean dispenserWeightedPercentage(int basePct) {
        return dispenserWeightedPercentage(basePct, 0.1f);
    }

    public boolean dispenserWeightedPercentage(int basePct, float dispenserWeight) {
        return getWorld().rand.nextInt(100) < basePct * (1 + getUpgrades(EnumUpgrade.DISPENSER) * dispenserWeight);
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
