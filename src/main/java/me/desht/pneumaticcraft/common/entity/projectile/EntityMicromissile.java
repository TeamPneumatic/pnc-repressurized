package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;

public class EntityMicromissile extends ThrowableEntity {
    private static final double SEEK_RANGE = 24;

    private static final DataParameter<Integer> TARGET_ID = EntityDataManager.defineId(EntityMicromissile.class, DataSerializers.INT);
    private static final DataParameter<Float> MAX_VEL_SQ = EntityDataManager.defineId(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ACCEL = EntityDataManager.defineId(EntityMicromissile.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> TURN_SPEED = EntityDataManager.defineId(EntityMicromissile.class, DataSerializers.FLOAT);

    private Entity targetEntity = null;

    private float maxVelocitySq = 0.5f;
    private float accel = 1.05f; // straight line acceleration
    private float turnSpeed = 0.1f;
    private float explosionPower = 2f;
    private EntityFilter entityFilter;
    private boolean outOfFuel = false;
    private FireMode fireMode = FireMode.SMART;

    public EntityMicromissile(EntityType<EntityMicromissile> type, World worldIn) {
        super(type, worldIn);
    }

    public EntityMicromissile(World worldIn, LivingEntity thrower, ItemStack iStack) {
        super(ModEntities.MICROMISSILE.get(), thrower, worldIn);

        if (iStack.hasTag()) {
            CompoundNBT tag = iStack.getTag();
            entityFilter = EntityFilter.fromString(tag.getString(ItemMicromissiles.NBT_FILTER));
            fireMode = FireMode.fromString(tag.getString(ItemMicromissiles.NBT_FIRE_MODE));
            switch (fireMode) {
                case SMART:
                    accel = Math.max(1.02f, 1.0f + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) / 10f);
                    maxVelocitySq = (float) Math.pow(0.25 + tag.getFloat(ItemMicromissiles.NBT_TOP_SPEED) * 3.75f, 2);
                    turnSpeed = 0.4f * tag.getFloat(ItemMicromissiles.NBT_TURN_SPEED);
                    explosionPower = Math.max(1f, 5 * tag.getFloat(ItemMicromissiles.NBT_DAMAGE));
                    break;
                case DUMB:
                    accel = 1.5f;
                    maxVelocitySq = 6.25f;
                    turnSpeed = 0f;
                    explosionPower = 3f;
                    break;
            }
        }
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TARGET_ID, 0);
        entityData.define(MAX_VEL_SQ, 0.5f);
        entityData.define(ACCEL, 1.05f);
        entityData.define(TURN_SPEED, 0.4f);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        if (getCommandSenderWorld().isClientSide) {
            if (key.equals(MAX_VEL_SQ)) {
                maxVelocitySq = entityData.get(MAX_VEL_SQ);
            } else if (key.equals(TARGET_ID)) {
                int id = entityData.get(TARGET_ID);
                targetEntity = id > 0 ? getCommandSenderWorld().getEntity(entityData.get(TARGET_ID)) : null;
            } else if (key.equals(ACCEL)) {
                accel = entityData.get(ACCEL);
            } else if (key.equals(TURN_SPEED)) {
                turnSpeed = entityData.get(TURN_SPEED);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount == 1) {
            if (getCommandSenderWorld().isClientSide) {
                getCommandSenderWorld().playLocalSound(getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1.0f, 0.8f, true);
            } else {
                entityData.set(MAX_VEL_SQ, maxVelocitySq);
                entityData.set(ACCEL, accel);
                entityData.set(TURN_SPEED, turnSpeed);
            }
        }

        if (tickCount > PNCConfig.Common.Micromissiles.lifetime) {
            outOfFuel = true;
        }

        if (!outOfFuel) {
            // negate default slowdown of projectiles applied in superclass
            if (this.isInWater()) {
                setDeltaMovement(getDeltaMovement().scale(1.25));
            } else {
                setDeltaMovement(getDeltaMovement().scale(1 / 0.99));
            }

            if ((targetEntity == null || !targetEntity.isAlive()) && fireMode == FireMode.SMART && !getCommandSenderWorld().isClientSide && (tickCount & 0x3) == 0) {
                targetEntity = tryFindNewTarget();
            }

            if (targetEntity != null) {
                // turn toward the target
                Vector3d diff = targetEntity.position().add(0, targetEntity.getEyeHeight(), 0).subtract(position()).normalize().scale(turnSpeed);
                setDeltaMovement(getDeltaMovement().add(diff));
            }

            // accelerate up to max velocity but cap there
            double velSq = getDeltaMovement().lengthSqr();//motionX * motionX + motionY * motionY + motionZ * motionZ;
            double mul = velSq > maxVelocitySq ? maxVelocitySq / velSq : accel;
            setDeltaMovement(getDeltaMovement().scale(mul));

            if (getCommandSenderWorld().isClientSide && getCommandSenderWorld().random.nextBoolean()) {
                Vector3d m = getDeltaMovement();
                level.addParticle(AirParticleData.DENSE, getX(), getY(), getZ(), -m.x/2, -m.y/2, -m.z/2);
            }
        }
    }

    private Entity tryFindNewTarget() {
        AxisAlignedBB aabb = new AxisAlignedBB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(SEEK_RANGE);
        List<Entity> l = getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, aabb, EntityPredicates.ENTITY_STILL_ALIVE);
        l.sort(new TargetSorter());
        Entity tgt = null;
        // find the closest entity which matches this missile's entity filter
        for (Entity e : l) {
            if (isValidTarget(e) && e.distanceToSqr(this) < SEEK_RANGE * SEEK_RANGE) {
                RayTraceContext ctx = new RayTraceContext(position(), e.position().add(0, e.getEyeHeight(), 0), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, e);
                RayTraceResult res = getCommandSenderWorld().clip(ctx);
                if (res.getType() == RayTraceResult.Type.MISS || res.getType() == RayTraceResult.Type.ENTITY) {
                    tgt = e;
                    break;
                }
            }
        }
        entityData.set(TARGET_ID, tgt == null ? 0 : tgt.getId());
        return tgt;
    }

    public boolean isValidTarget(Entity e) {
        // never target the player who fired the missile or any of their pets/drones
        Entity thrower = getOwner();  // getThrower()
        if (thrower != null) {
            if (e.equals(thrower)
                    || e instanceof TameableEntity && thrower.equals(((TameableEntity) e).getOwner())
                    || e instanceof EntityDrone && thrower.getUUID().equals(((EntityDrone) e).getOwnerUUID())
                    || e instanceof HorseEntity && thrower.getUUID().equals(((HorseEntity) e).getOwnerUUID())) {
                return false;
            }
        }

        if (entityFilter != null && !entityFilter.test(e)) {
            return false;
        }

        return e instanceof LivingEntity || e instanceof BoatEntity || e instanceof AbstractMinecartEntity;
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (tickCount > 5 && !getCommandSenderWorld().isClientSide && isAlive()) {
            explode(result instanceof EntityRayTraceResult ? ((EntityRayTraceResult) result).getEntity() : null);
        }
    }

    private void explode(Entity e) {
        remove();
        Explosion.Mode mode = PNCConfig.Common.Micromissiles.damageTerrain ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
        double x, y, z;
        if (e == null) {
            x = getX();
            y = getY();
            z = getZ();
        } else {
            // make the explosion closer to the target entity (a fast projectile's position could be a little distance away)
            x = MathHelper.lerp(0.25f, e.getX(), getX());
            y = MathHelper.lerp(0.25f, e.getY(), getY());
            z = MathHelper.lerp(0.25f, e.getZ(), getZ());
        }
        getCommandSenderWorld().explode(this, x, y, z, (float) PNCConfig.Common.Micromissiles.baseExplosionDamage * explosionPower, false, mode);
    }

    // shoot()
    @Override
    public void shootFromRotation(Entity entityThrower, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float y = -MathHelper.sin(pitch * 0.017453292F);
        float z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        this.shoot(x, y, z, velocity, 0f);
        setDeltaMovement(getDeltaMovement().add(entityThrower.getDeltaMovement().x, 0, entityThrower.getDeltaMovement().z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x = x / f * velocity;
        y = y / f * velocity;
        z = z / f * velocity;
        setDeltaMovement(x, y, z);

        float f1 = MathHelper.sqrt(x * x + z * z);
        this.yRot = (float)(MathHelper.atan2(x, z) * (180D / Math.PI));
        this.xRot = (float)(MathHelper.atan2(y, f1) * (180D / Math.PI));
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    @Override
    protected float getGravity() {
        return outOfFuel ? super.getGravity() : 0f;
    }

    @Override
    public boolean isNoGravity() {
        return !outOfFuel;
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("turnSpeed", turnSpeed);
        compound.putFloat("explosionScaling", explosionPower);
        compound.putFloat("topSpeedSq", maxVelocitySq);
        compound.putString("filter", entityFilter.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        turnSpeed = compound.getFloat("turnSpeed");
        explosionPower = compound.getFloat("explosionScaling");
        maxVelocitySq = compound.getFloat("topSpeedSq");
        entityFilter = EntityFilter.fromString(compound.getString("filter"));
    }

    public void setTarget(Entity target) {
        targetEntity = target;
    }

    private class TargetSorter implements Comparator<Entity> {
        private final Vector3d vec;

        TargetSorter() {
            vec = position();
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(vec.distanceToSqr(e1.position()), vec.distanceToSqr(e2.position()));
        }
    }
}
