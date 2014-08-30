package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.world.World;
import pneumaticCraft.common.entity.projectile.EntityChopperSeeds;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class BlockChopperPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.CHOPPER_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_CHOPPER_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote) {
            EntityChopperSeeds plant = new EntityChopperSeeds(world, x + 0.5D, y + 0.8D, z + 0.5D);
            // plant.motionX = (rand.nextFloat() - 0.5F);
            plant.motionY = 0.3F;
            // plant.motionZ = (rand.nextFloat() - 0.5F);
            world.spawnEntityInWorld(plant);
            plant.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
        }
    }

    @Override
    protected float getGrowthRate(World world, int x, int y, int z){
        return super.getGrowthRate(world, x, y, z) * 0.25F;
    }
}
