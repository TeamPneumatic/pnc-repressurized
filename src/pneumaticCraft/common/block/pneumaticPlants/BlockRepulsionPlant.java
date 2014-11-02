package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class BlockRepulsionPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.REPULSION_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_REPULSION_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote) {
            ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.REPULSION_PLANT_DAMAGE);
            EntityItem plant = new EntityItem(world, x + 0.5D, y + 0.8D, z + 0.5D, seed);
            plant.motionX = (rand.nextFloat() - 0.5F) / 2;
            plant.motionY = 0.7F;
            plant.motionZ = (rand.nextFloat() - 0.5F) / 2;
            plant.lifespan = 300;
            ItemPlasticPlants.markInactive(plant);
            world.spawnEntityInWorld(plant);
            plant.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
        }
    }
}
