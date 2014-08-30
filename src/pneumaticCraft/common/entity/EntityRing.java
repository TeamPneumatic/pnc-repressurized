package pneumaticCraft.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import pneumaticCraft.client.render.RenderRing;

public class EntityRing extends Entity{

    public RenderRing ring, oldRing;
    private final Entity targetEntity;
    public final int color;

    public EntityRing(World par1World, double startX, double startY, double startZ, Entity targetEntity, int color){
        super(par1World);
        posX = lastTickPosX = startX;
        posY = lastTickPosY = startY;
        posZ = lastTickPosZ = startZ;
        this.targetEntity = targetEntity;
        this.color = color;

        double dx = targetEntity.posX - posX;
        double dy = targetEntity.posY - posY;
        double dz = targetEntity.posZ - posZ;
        float f = MathHelper.sqrt_double(dx * dx + dz * dz);
        prevRotationYaw = rotationYaw = (float)(Math.atan2(dx, dz) * 180.0D / Math.PI);
        prevRotationPitch = rotationPitch = (float)(Math.atan2(dy, f) * 180.0D / Math.PI);
        renderDistanceWeight = 10.0D;
        ignoreFrustumCheck = true;
    }

    @Override
    public void onUpdate(){
        double endX = targetEntity.posX;
        double endY = targetEntity.posY;
        double endZ = targetEntity.posZ;
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        if(ring == null) {
            ring = new RenderRing(posX, posY, posZ, endX, endY, endZ, color);
        } else {
            if(oldRing == null) {
                oldRing = new RenderRing(ring.startX, ring.startY, ring.startZ, ring.endX, ring.endY, ring.endZ, color);
            } else {
                oldRing.endX = ring.endX;
                oldRing.endY = ring.endY;
                oldRing.endZ = ring.endZ;
            }
            ring.endX = endX;
            ring.endY = endY;
            ring.endZ = endZ;

            double dx = endX - posX;
            double dy = endY - posY;
            double dz = endZ - posZ;
            float f = MathHelper.sqrt_double(dx * dx + dz * dz);
            rotationYaw = (float)(Math.atan2(dx, dz) * 180.0D / Math.PI);
            rotationPitch = (float)(Math.atan2(dy, f) * 180.0D / Math.PI);

            oldRing.setProgress(ring.getProgress());
            if(ring.incProgress(0.05F)) {
                setDead();
            }
        }
    }

    @Override
    protected void entityInit(){}

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1){}

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1){}

}
