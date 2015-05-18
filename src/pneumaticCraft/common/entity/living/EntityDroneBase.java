package pneumaticCraft.common.entity.living;

import net.minecraft.entity.EntityCreature;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.render.RenderLaser;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class EntityDroneBase extends EntityCreature{
    public float oldPropRotation;
    public float propRotation;
    public float laserExtension; //How far the laser comes out of the drone. 1F is fully extended
    public float oldLaserExtension;
    @SideOnly(Side.CLIENT)
    protected RenderLaser digLaser;

    public EntityDroneBase(World world){
        super(world);
    }

    public void renderExtras(double x, double y, double z, float partialTicks){
        ChunkPosition diggingPos = getDugBlock();
        if(diggingPos != null) {
            if(digLaser == null) {
                int color = 0xFF0000;

                digLaser = new RenderLaser(color);
            }
            digLaser.render(partialTicks, 0, getLaserOffsetY(), 0, diggingPos.chunkPosX + 0.5 - posX, diggingPos.chunkPosY + 0.45 - posY, diggingPos.chunkPosZ + 0.5 - posZ);
        }
    }

    protected double getLaserOffsetY(){
        return 0.05;
    }

    public int getLaserColor(){
        return 0xFF0000;
    }

    public int getDroneColor(){
        return 0;
    }

    public boolean isAccelerating(){
        return true;
    }

    protected abstract ChunkPosition getDugBlock();

}
