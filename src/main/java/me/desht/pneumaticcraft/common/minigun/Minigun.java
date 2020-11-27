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
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class Minigun {
    public static final float MAX_GUN_SPEED = 0.4f;
    private static final float MAX_GUN_YAW_CHANGE = 10f;
    private static final float MAX_GUN_PITCH_CHANGE = 10f;

    private final boolean requiresTarget;

    private float minigunSpeed;
    private int minigunTriggerTimeOut;
    private int minigunSoundCounter = -1;
    private final Random rand = new Random();
    private float minigunRotation, oldMinigunRotation;
    public float minigunYaw, oldMinigunYaw;
    public float minigunPitch, oldMinigunPitch;
    private boolean sweeping; // When true, the yaw of the minigun will sweep with a sinus pattern when not targeting.
    private boolean returning; // When true, the minigun is returning to its idle position.
    private double sweepingProgress;

    private boolean gunAimedAtTarget;

    private LazyOptional<? extends IAirHandler> airCapability = LazyOptional.empty();
    private int airUsage;
    protected ItemStack minigunStack = ItemStack.EMPTY;
    private ItemStack ammoStack = ItemStack.EMPTY;
    protected PlayerEntity player;
    protected World world;
    private LivingEntity attackTarget;
    private float idleYaw;

    public Minigun(boolean requiresTarget) {
        this.requiresTarget = requiresTarget;
    }

    public Minigun setAirHandler(LazyOptional<? extends IAirHandler> airHandler, int airUsage) {
        this.airCapability = airHandler;
        this.airUsage = airUsage;
        return this;
    }

    public Minigun setItemStack(@Nonnull ItemStack stack) {
        this.minigunStack = stack;
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

    public Minigun setPlayer(PlayerEntity player) {
        this.player = player;
        return this;
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
        return stack.isEmpty() ? 0xFF313131 : Minecraft.getInstance().getItemColors().getColor(stack, 1);
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
    public Object getSoundSource() {
        return player;
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

    public int getMinigunSoundCounter() {
        return minigunSoundCounter;
    }

    public void setMinigunSoundCounter(int minigunSoundCounter) {
        this.minigunSoundCounter = minigunSoundCounter;
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

    public float getIdleYaw() {
        return idleYaw;
    }

    public void setIdleYaw(float idleYaw) {
        this.idleYaw = idleYaw;
    }

    public boolean isGunAimedAtTarget() {
        return gunAimedAtTarget;
    }

    public boolean tryFireMinigun(Entity target) {
        boolean lastShotOfAmmo = false;
        if (!ammoStack.isEmpty() && ammoStack.getDamage() < ammoStack.getMaxDamage() && airCapability.map(h -> h.getPressure() > 0).orElse(true)) {
            setMinigunTriggerTimeOut(Math.max(10, getMinigunSoundCounter()));
            if (getMinigunSpeed() == MAX_GUN_SPEED && (!requiresTarget || gunAimedAtTarget)) {
                RayTraceResult rtr = null;
                ItemGunAmmo ammoItem = (ItemGunAmmo) ammoStack.getItem();
                if (!requiresTarget) {
                    rtr = PneumaticCraftUtils.getMouseOverServer(player, getRange());
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
        if (!world.isRemote) {
            setMinigunActivated(getMinigunTriggerTimeOut() > 0);

            setAmmoColorStack(ammoStack);

            if (getMinigunTriggerTimeOut() > 0) {
                setMinigunTriggerTimeOut(getMinigunTriggerTimeOut() - 1);
                if (getMinigunSpeed() == 0) {
                    playSound(ModSounds.HUD_INIT.get(), 3, 0.9F);
                }
            }
            if (getMinigunSoundCounter() == 0 && getMinigunTriggerTimeOut() == 0) {
                playSound(ModSounds.MINIGUN_STOP.get(), 3, 0.5F);
                setMinigunSoundCounter(-1);
            }
        }
        if (isMinigunActivated()) {
            float speedBonus = getUpgrades(EnumUpgrade.SPEED) * 0.0033F;
            float lastSpeed = getMinigunSpeed();
            setMinigunSpeed(Math.min(getMinigunSpeed() + 0.01F + speedBonus, MAX_GUN_SPEED));
            if (getMinigunSpeed() > lastSpeed && getMinigunSpeed() >= MAX_GUN_SPEED && !world.isRemote) {
                NetworkHandler.sendToAllTracking(new PacketPlayMovingSound(MovingSounds.Sound.MINIGUN, getSoundSource()), player.world, player.getPosition());
            }
        } else {
            setMinigunSpeed(Math.max(0F, getMinigunSpeed() - 0.003F));
        }

        setMinigunRotation(getMinigunRotation() + getMinigunSpeed());

        double targetYaw;
        double targetPitch = 0;
        if (attackTarget != null) {
            double deltaX = posX - attackTarget.getPosX();
            double deltaZ = posZ - attackTarget.getPosZ();

            if (deltaX >= 0 && deltaZ < 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D;
            } else if (deltaX >= 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 90;
            } else if (deltaX < 0 && deltaZ >= 0) {
                targetYaw = Math.atan(Math.abs(deltaX / deltaZ)) / Math.PI * 180D + 180;
            } else {
                targetYaw = Math.atan(Math.abs(deltaZ / deltaX)) / Math.PI * 180D + 270;
            }
            if (targetYaw - minigunYaw > 180) {
                targetYaw -= 360;
            } else if (minigunYaw - targetYaw > 180) {
                targetYaw += 360;
            }
            targetPitch = Math.toDegrees(Math.atan((posY - attackTarget.getPosY() - attackTarget.getHeight() / 2) / PneumaticCraftUtils.distBetween(posX, posZ, attackTarget.getPosX(), attackTarget.getPosZ())));

            minigunPitch = moveToward(minigunPitch, (float) targetPitch, MAX_GUN_PITCH_CHANGE);
            minigunYaw = minigunPitch < -80 || minigunPitch > 80 ? (float) targetYaw : moveToward(minigunYaw, (float) targetYaw, MAX_GUN_YAW_CHANGE);
            gunAimedAtTarget = MathHelper.epsilonEquals(minigunYaw, targetYaw) && MathHelper.epsilonEquals(minigunPitch, targetPitch);
        } else if (isReturning()) {
            minigunYaw = moveToward(minigunYaw, idleYaw, MAX_GUN_YAW_CHANGE);
            if (PneumaticCraftUtils.areFloatsEqual(minigunYaw, idleYaw)) {
                setReturning(false);
            }
            minigunPitch = moveToward(minigunPitch, (float) targetPitch, MAX_GUN_PITCH_CHANGE);
        } else if (isSweeping()) {
            minigunYaw -= Math.cos(sweepingProgress) * 22;
            sweepingProgress += 0.05D;
            minigunYaw += Math.cos(sweepingProgress) * 22;
            minigunPitch = moveToward(minigunPitch, (float) targetPitch, MAX_GUN_PITCH_CHANGE);
        }

        if (!world.isRemote && isMinigunActivated() && getMinigunSpeed() == MAX_GUN_SPEED
                && (!requiresTarget || gunAimedAtTarget && attackTarget != null)) {
            if (getMinigunSoundCounter() <= 0) {
                setMinigunSoundCounter(20);
            }
        }
        if (getMinigunSoundCounter() > 0) setMinigunSoundCounter(getMinigunSoundCounter() - 1);
    }

    private float moveToward(float val, float target, float amount) {
        if (val > target) {
            val = Math.max(val - amount, target);
        } else {
            val = Math.min(val + amount, target);
        }
        return val;
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
}
