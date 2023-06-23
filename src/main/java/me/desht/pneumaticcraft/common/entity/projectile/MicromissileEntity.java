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

package me.desht.pneumaticcraft.common.entity.projectile;

import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModEntityTypes;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem.FireMode;
import me.desht.pneumaticcraft.common.particle.AirParticleData;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MicromissileEntity extends ThrowableProjectile {
    private static final double SEEK_RANGE = 24;

    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(MicromissileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MAX_VEL_SQ = SynchedEntityData.defineId(MicromissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ACCEL = SynchedEntityData.defineId(MicromissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TURN_SPEED = SynchedEntityData.defineId(MicromissileEntity.class, EntityDataSerializers.FLOAT);

    private Entity targetEntity = null;

    private float maxVelocitySq = 0.5f;
    private float accel = 1.05f; // straight line acceleration
    private float turnSpeed = 0.1f;
    private float explosionPower = 2f;
    private EntityFilter entityFilter;
    private boolean outOfFuel = false;
    private FireMode fireMode = FireMode.SMART;

    public MicromissileEntity(EntityType<MicromissileEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public MicromissileEntity(Level worldIn, LivingEntity thrower, ItemStack iStack) {
        super(ModEntityTypes.MICROMISSILE.get(), thrower, worldIn);

        if (iStack.hasTag()) {
            CompoundTag tag = Objects.requireNonNull(iStack.getTag());
            entityFilter = EntityFilter.fromString(tag.getString(MicromissilesItem.NBT_FILTER));
            fireMode = FireMode.fromString(tag.getString(MicromissilesItem.NBT_FIRE_MODE));
            switch (fireMode) {
                case SMART -> {
                    accel = Math.max(1.02f, 1.0f + tag.getFloat(MicromissilesItem.NBT_TOP_SPEED) / 10f);
                    maxVelocitySq = (float) Math.pow(0.25 + tag.getFloat(MicromissilesItem.NBT_TOP_SPEED) * 3.75f, 2);
                    turnSpeed = 0.4f * tag.getFloat(MicromissilesItem.NBT_TURN_SPEED);
                    explosionPower = Math.max(1f, 5 * tag.getFloat(MicromissilesItem.NBT_DAMAGE));
                }
                case DUMB -> {
                    accel = 1.5f;
                    maxVelocitySq = 6.25f;
                    turnSpeed = 0f;
                    explosionPower = 3f;
                }
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
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
                getCommandSenderWorld().playLocalSound(getX(), getY(), getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 0.8f, true);
            } else {
                entityData.set(MAX_VEL_SQ, maxVelocitySq);
                entityData.set(ACCEL, accel);
                entityData.set(TURN_SPEED, turnSpeed);
            }
        } else if (tickCount > ConfigHelper.common().micromissiles.lifetime.get()) {
            outOfFuel = true;
            if (tickCount > ConfigHelper.common().micromissiles.maxLifetime.get()) {
                explode(null);
            }
        }

        if (!outOfFuel) {
            // undo default slowdown of projectiles applied in super.tick()
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
                Vec3 diff = targetEntity.position().add(0, targetEntity.getEyeHeight(), 0).subtract(position()).normalize().scale(turnSpeed);
                setDeltaMovement(getDeltaMovement().add(diff));
            }

            // accelerate up to max velocity but cap there
            double velSq = getDeltaMovement().lengthSqr();
            double mul = velSq > maxVelocitySq ? maxVelocitySq / velSq : accel;
            setDeltaMovement(getDeltaMovement().scale(mul));

            if (getCommandSenderWorld().isClientSide && getCommandSenderWorld().random.nextBoolean()) {
                Vec3 m = getDeltaMovement();
                level.addParticle(AirParticleData.DENSE, getX(), getY(), getZ(), -m.x/2, -m.y/2, -m.z/2);
            }
        }
    }

    private Entity tryFindNewTarget() {
        AABB aabb = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(SEEK_RANGE);
        List<LivingEntity> l = getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE);
        l.sort(new TargetSorter());
        Entity tgt = null;
        // find the closest entity which matches this missile's entity filter
        for (Entity e : l) {
            if (isValidTarget(e) && e.distanceToSqr(this) < SEEK_RANGE * SEEK_RANGE) {
                ClipContext ctx = new ClipContext(position(), e.position().add(0, e.getEyeHeight(), 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e);
                HitResult res = getCommandSenderWorld().clip(ctx);
                if (res.getType() == HitResult.Type.MISS || res.getType() == HitResult.Type.ENTITY) {
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
        Entity thrower = getOwner();
        if (thrower != null) {
            UUID throwerID = thrower.getUUID();
            if (thrower.equals(e)
                    || e instanceof TamableAnimal t && throwerID.equals(t.getOwnerUUID())
                    || e instanceof DroneEntity d && throwerID.equals(d.getOwnerUUID())
                    || e instanceof Horse h && throwerID.equals(h.getOwnerUUID())) {
                return false;
            }
        }

        if (entityFilter != null && !entityFilter.test(e)) {
            return false;
        }

        return e instanceof LivingEntity || e instanceof Boat || e instanceof AbstractMinecart;
    }

    @Override
    protected void onHit(HitResult result) {
        if (tickCount > 5 && !getCommandSenderWorld().isClientSide && isAlive()) {
            explode(result instanceof EntityHitResult ? ((EntityHitResult) result).getEntity() : null);
        }
    }

    private void explode(Entity e) {
        discard();

        Level.ExplosionInteraction mode = ConfigHelper.common().micromissiles.damageTerrain.get() ?
                Level.ExplosionInteraction.TNT :
                Level.ExplosionInteraction.NONE;
        boolean fire = ConfigHelper.common().micromissiles.startFires.get();
        float radius = ConfigHelper.common().micromissiles.baseExplosionDamage.get().floatValue() * explosionPower;

        double x, y, z;
        if (e == null) {
            x = getX();
            y = getY();
            z = getZ();
        } else {
            // make the explosion closer to the target entity (a fast projectile's position could be a little distance away)
            x = Mth.lerp(0.25f, e.getX(), getX());
            y = Mth.lerp(0.25f, e.getY(), getY());
            z = Mth.lerp(0.25f, e.getZ(), getZ());
        }
        getCommandSenderWorld().explode(this, x, y, z, radius, fire, mode);
    }

    @Override
    public void shootFromRotation(Entity entityThrower, float pitch, float yaw, float pitchOffset, float velocity, float inaccuracy) {
        float x = -Mth.sin(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
        float y = -Mth.sin(pitch * 0.017453292F);
        float z = Mth.cos(yaw * 0.017453292F) * Mth.cos(pitch * 0.017453292F);
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

        float f1 = Mth.sqrt((float) (x * x + z * z));
        this.setYRot((float)(Mth.atan2(x, z) * (180D / Math.PI)));
        this.setXRot((float)(Mth.atan2(y, f1) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("turnSpeed", turnSpeed);
        compound.putFloat("explosionScaling", explosionPower);
        compound.putFloat("topSpeedSq", maxVelocitySq);
        compound.putString("filter", entityFilter.toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
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
        private final Vec3 vec;

        TargetSorter() {
            vec = position();
        }

        @Override
        public int compare(Entity e1, Entity e2) {
            return Double.compare(vec.distanceToSqr(e1.position()), vec.distanceToSqr(e2.position()));
        }
    }
}
