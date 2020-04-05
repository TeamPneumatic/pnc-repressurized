package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.client.render.RenderRing;
import me.desht.pneumaticcraft.common.core.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityRing extends Entity {

    public RenderRing ring, oldRing;
    private final Entity targetEntity;
    public final int color;

    public EntityRing(EntityType<EntityRing> type, World world) {
        super(type, world);

        targetEntity = null;
        color = 0;
    }

    public EntityRing(World par1World, double startX, double startY, double startZ, Entity targetEntity, int color) {
        super(ModEntities.RING.get(), par1World);

        setPosition(startX, startY, startZ);
        lastTickPosX = startX;
        lastTickPosY = startY;
        lastTickPosZ = startZ;
        this.targetEntity = targetEntity;
        this.color = color;

        double dx = targetEntity.getPosX() - getPosX();
        double dy = targetEntity.getPosY() - getPosY();
        double dz = targetEntity.getPosZ() - getPosZ();
        float f = MathHelper.sqrt(dx * dx + dz * dz);
        prevRotationYaw = rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float) (Math.atan2(dy, f) * 180.0D / Math.PI);
        ignoreFrustumCheck = true;
        if (par1World.isRemote) {
            setRenderDistanceWeight(10.0D);
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if (targetEntity == null) return;

        Vec3d end = targetEntity.getPositionVector();
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        if (ring == null) {
            ring = new RenderRing(getPosX(), getPosY(), getPosZ(), end.x, end.y, end.z, color);
        } else {
            if (oldRing == null) {
                oldRing = new RenderRing(ring.startX, ring.startY, ring.startZ, ring.endX, ring.endY, ring.endZ, color);
            } else {
                oldRing.endX = ring.endX;
                oldRing.endY = ring.endY;
                oldRing.endZ = ring.endZ;
            }
            ring.endX = end.x;
            ring.endY = end.y;
            ring.endZ = end.z;

            double dx = end.x - getPosX();
            double dy = end.y - getPosY();
            double dz = end.z - getPosZ();
            float f = MathHelper.sqrt(dx * dx + dz * dz);
            rotationYaw = (float) (Math.atan2(dx, dz) * 180.0D / Math.PI);
            rotationPitch = (float) (Math.atan2(dy, f) * 180.0D / Math.PI);

            oldRing.setProgress(ring.getProgress());
            if (ring.incProgress(0.05F)) {
                remove();
            }
        }
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
    }

}
