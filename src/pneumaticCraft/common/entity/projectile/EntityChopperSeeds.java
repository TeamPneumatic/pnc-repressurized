package pneumaticCraft.common.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;

public class EntityChopperSeeds extends Entity{
    int randExplodeTime;

    public EntityChopperSeeds(World par1World){
        super(par1World);
        randExplodeTime = rand.nextInt(20) + 60;
    }

    public EntityChopperSeeds(World par1World, double par2, double par4, double par6){
        this(par1World);
        setPosition(par2, par4, par6);
    }

    @Override
    protected void entityInit(){}

    @Override
    public void onUpdate(){
        motionY -= 0.005D;
        moveEntity(motionX, motionY, motionZ);
        super.onUpdate();
        if(ticksExisted > randExplodeTime && !worldObj.isRemote) {
            int deltaTick = ticksExisted - randExplodeTime;
            EntityItem seed = new EntityItem(worldObj, posX, posY, posZ, new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.CHOPPER_PLANT_DAMAGE));
            seed.motionY = motionY;
            seed.motionX = Math.sin(0.5D * Math.PI * deltaTick + ticksExisted * 0.2D);
            seed.motionZ = Math.cos(0.5D * Math.PI * deltaTick + ticksExisted * 0.2D);
            seed.lifespan = 300;
            ItemPlasticPlants.markInactive(seed);
            worldObj.spawnEntityInWorld(seed);
            if(deltaTick > 3) setDead();
        }

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag){

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag){

    }

}
