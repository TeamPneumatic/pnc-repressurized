package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class EntityMicromissile extends EntityThrowable {
    private static final double SEEK_RANGE = 24;

    private static final DataParameter<Integer> TARGET_ID = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.VARINT);
    private static final DataParameter<Float> MAX_VEL_SQ = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ACCEL = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> TURN_SPEED = EntityDataManager.createKey(EntityMicromissile.class, DataSerializers.FLOAT);

    private Entity targetEntity = null;

    private float maxVelocitySq = 0.5f;
    private float accel = 1.05f; // straight line acceleration
    private float turnSpeed = 0.1f;
    private float explosionPower = 2f;
    private String entityFilter = "";
    private boolean outOfFuel = false;
    private FireMode fireMode = FireMode.SMART;

    public EntityMicromissile(World worldIn) {
        super(worldIn);
    }

    public EntityMicromissile(World worldIn, EntityLivingBase thrower, ItemStack iStack) {
        super(worldIn, thrower);

        if (iStack.hasTagCompound()) {
            NBTTagCompound tag = iStack.getTagCompound();
            entityFilter = tag.getString(ItemMicromissiles.NBT_FILTER);
            fireMode = FireMode.fromString(tag.getString(ItemMicromissiles.NBT_FIRE_MODE));
            switch (fireMode) {
                case SMART:
                    accel = Math.max(1.02f, 1.0f + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) / 10f);
                    maxVelocitySq = (float) Math.pow(0.25 + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) * 3.75f, 2);
                    turnSpeed = 0.4f * tag.getFloat(ItemMicromissiles.NBT_TURN_SPEED);
                    explosionPower = Math.max(1f, 5 * tag.getFloat(ItemMicromissiles.NBT_DAMAGE));
                    break;
                case DUMB:
                    accel = 1.02f;
                    maxVelocitySq = 2f;
                    turnSpeed = 0f;
                    explosionPower = 3f;
                    break;
            }
        }
    }

    public EntityMicromissile(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        dataManager.register(TARGET_ID, 0);
        dataManager.register(MAX_VEL_SQ, 0.5f);
        dataManager.register(ACCEL, 1.05f);
        dataManager.register(TURN_SPEED, 0.4f);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (getEntityWorld().isRemote) {
            if (key.equals(MAX_VEL_SQ)) {
                maxVelocitySq = dataManager.get(MAX_VEL_SQ);
            } else if (key.equals(TARGET_ID)) {
                int id = dataManager.get(TARGET_ID);
                targetEntity = id > 0 ? getEntityWorld().getEntityByID(dataManager.get(TARGET_ID)) : null;
            } else if (key.equals(ACCEL)) {
                accel = dataManager.get(ACCEL);
            } else if (key.equals(TURN_SPEED)) {
                turnSpeed = dataManager.get(TURN_SPEED);
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (ticksExisted == 1) {
            if (getEntityWorld().isRemote) {
                getEntityWorld().playSound(posX, posY, posZ, SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.PLAYERS, 1.0f, 0.8f, true);
            } else {
                dataManager.set(MAX_VEL_SQ, maxVelocitySq);
                dataManager.set(ACCEL, accel);
                dataManager.set(TURN_SPEED, turnSpeed);
            }
        }

        if (ticksExisted > ConfigHandler.microMissile.lifetime) {
            outOfFuel = true;
        }

        if (!outOfFuel) {
            // negate default slowdown of projectiles applied in superclass
            if (this.isInWater()) {
                motionX *= 1.25;
                motionY *= 1.25;
                motionZ *= 1.25;
            } else {
                motionX *= 1 / 0.99;
                motionY *= 1 / 0.99;
                motionZ *= 1 / 0.99;
            }

            if ((targetEntity == null || targetEntity.isDead) && fireMode == FireMode.SMART && !getEntityWorld().isRemote && (ticksExisted & 0x3) == 0) {
                targetEntity = tryFindNewTarget();
            }

            if (targetEntity != null) {
                // turn toward the target
                Vec3d diff = targetEntity.getPositionVector().add(0, targetEntity.getEyeHeight(), 0).subtract(getPositionVector()).normalize().scale(turnSpeed);
                motionX += diff.x;
                motionY += diff.y;
                motionZ += diff.z;
            }

            // accelerate up to max velocity but cap there
            double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            double mul = velSq > maxVelocitySq ? maxVelocitySq / velSq : accel;
            motionX *= mul;
            motionY *= mul;
            motionZ *= mul;

            if (getEntityWorld().isRemote) {
                PneumaticCraftRepressurized.proxy.playCustomParticle(EnumCustomParticleType.AIR_PARTICLE_DENSE, getEntityWorld(), posX, posY, posZ, -motionX/2, -motionY/2, -motionZ/2);
            }
        }
    }

    private Entity tryFindNewTarget() {
        AxisAlignedBB aabb = new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ).grow(SEEK_RANGE);
        List<Entity> l = getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, aabb, EntitySelectors.IS_ALIVE);
        l.sort(new TargetSorter());
        Entity tgt = null;
        // find the closest entity which matches this missile's entity filter
        for (Entity e : l) {
            if (isValidTarget(e) && e.getDistanceSq(this) < SEEK_RANGE * SEEK_RANGE) {
                RayTraceResult res = getEntityWorld().rayTraceBlocks(getPositionVector(), e.getPositionVector().add(0, e.getEyeHeight(), 0), false, false, true);
                if (res == null || res.typeOfHit == RayTraceResult.Type.MISS || res.typeOfHit == RayTraceResult.Type.ENTITY) {
                    tgt = e;
                    break;
                }
            }
        }
        dataManager.set(TARGET_ID, tgt == null ? 0 : tgt.getEntityId());
        return tgt;
    }

    public boolean isValidTarget(Entity e) {
        // never target the player who fired the missile or any of their pets/drones
        if (thrower != null) {
            if (e.equals(thrower)
                    || e instanceof EntityTameable && thrower.equals(((EntityTameable) e).getOwner())
                    || e instanceof EntityDrone && thrower.getUniqueID().toString().equals(((EntityDrone) e).getOwnerUUID())
                    || e instanceof EntityHorse && thrower.getUniqueID().equals(((EntityHorse) e).getOwnerUniqueId())) {
                return false;
            }
        }

        if (!entityFilter.isEmpty() && !PneumaticCraftUtils.isEntityValidForFilter(entityFilter, e)) {
            return false;
        }

        return e instanceof EntityLivingBase || e instanceof EntityBoat || e instanceof EntityMinecart;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (ticksExisted > 5 && !getEntityWorld().isRemote && !isDead) {
            explode();
        }
    }

    private void explode() {
        setDead();
        getEntityWorld().createExplosion(this, posX, posY, posZ, ConfigHandler.microMissile.baseExplosionDamage * explosionPower, ConfigHandler.microMissile.damageTerrain);
    }

    @Override
    public void shoot(Entity entityThrower, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float y = -MathHelper.sin(pitch * 0.017453292F);
        float z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        this.shoot((double)x, (double)y, (double)z, velocity, 0f);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x = x / f * velocity;
        y = y / f * velocity;
        z = z / f * velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;

        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(y, (double)f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    @Override
    protected float getGravityVelocity() {
        return outOfFuel ? super.getGravityVelocity() : 0f;
    }

    @Override
    public boolean hasNoGravity() {
        return !outOfFuel;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("turnSpeed", turnSpeed);
        compound.setFloat("explosionScaling", explosionPower);
        compound.setFloat("topSpeedSq", maxVelocitySq);
        compound.setString("filter", entityFilter);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        turnSpeed = compound.getFloat("turnSpeed");
        explosionPower = compound.getFloat("explosionScaling");
        maxVelocitySq = compound.getFloat("topSpeedSq");
        entityFilter = compound.getString("filter");
    }

    public void setTarget(Entity target) {
        targetEntity = target;
    }

    private class TargetSorter implements Comparator<Entity> {
        private final Vec3d vec;

        TargetSorter() {
            vec = new Vec3d(posX, posY, posZ);
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(vec.squareDistanceTo(e1.getPositionVector()), vec.squareDistanceTo(e2.getPositionVector()));
        }
    }
}
