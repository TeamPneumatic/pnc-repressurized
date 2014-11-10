package pneumaticCraft.common.entity.projectile;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;

public class EntityPotionCloud extends Entity{
    private int age;
    private static final double START_EFFECT_RADIUS = 2D;
    private double radius;
    private static final int POTION_DATAWATCHER_ID = 25;

    public EntityPotionCloud(World par1World){
        super(par1World);
        radius = START_EFFECT_RADIUS;
        height = 0.5F;
    }

    public EntityPotionCloud(World world, double x, double y, double z){
        this(world);
        setPosition(x, y, z);
    }

    @Override
    protected void entityInit(){
        int potionID;
        do {
            potionID = rand.nextInt(Potion.potionTypes.length);
        } while(Potion.potionTypes[potionID] == null);
        dataWatcher.addObject(POTION_DATAWATCHER_ID, potionID);

    }

    private int getPotionID(){
        return dataWatcher.getWatchableObjectInt(POTION_DATAWATCHER_ID);
    }

    private void setPotionID(int ID){
        dataWatcher.updateObject(POTION_DATAWATCHER_ID, ID);
    }

    @Override
    public void onUpdate(){
        age++;
        radius -= 0.001D;
        if(radius <= 0.0D && !worldObj.isRemote) {
            EntityItem seed = new EntityItem(worldObj, posX, posY, posZ, new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.POTION_PLANT_DAMAGE));
            seed.lifespan = 300;
            ItemPlasticPlants.markInactive(seed);
            worldObj.spawnEntityInWorld(seed);
            setDead();
        }
        if(age % 60 == 0) {
            motionX += (rand.nextDouble() - 0.5D) * 0.1D;
            motionY += (rand.nextDouble() - 0.6D) * 0.1D;
            motionZ += (rand.nextDouble() - 0.5D) * 0.1D;
        }
        super.onUpdate();
        moveEntity(motionX, motionY, motionZ);

        if(worldObj.isRemote) {
            int potionColor = getPotionID() < Potion.potionTypes.length && Potion.potionTypes[getPotionID()] != null ? Potion.potionTypes[getPotionID()].getLiquidColor() : 0xFFFFFF;
            for(int i = 0; i < 4; i++)
                worldObj.spawnParticle("mobSpell", posX + (rand.nextDouble() - 0.5D) * 2 * radius, posY + (rand.nextDouble() - 0.5D) * 2 * radius, posZ + (rand.nextDouble() - 0.5D) * 2 * radius, (potionColor >> 16 & 255) / 255.0F, (potionColor >> 8 & 255) / 255.0F, (potionColor >> 0 & 255) / 255.0F);
        } else if(getPotionID() >= Potion.potionTypes.length) {
            setDead();
        }

        AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(posX - radius, posY - radius, posZ - radius, posX + radius, posY + radius, posZ + radius);
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, bbBox);
        for(EntityLivingBase entity : entities) {
            entity.addPotionEffect(new PotionEffect(getPotionID(), 200));
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag){
        age = tag.getInteger("age");
        radius = tag.getDouble("radius");
        setPotionID(tag.getInteger("potionID"));

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag){
        tag.setInteger("age", age);
        tag.setDouble("radius", radius);
        tag.setInteger("potionID", getPotionID());
    }

}
