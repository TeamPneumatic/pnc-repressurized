package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class BlockPropulsionPlant extends BlockPneumaticPlantBase{
    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_PROPULSION_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote) {
            ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.PROPULSION_PLANT_DAMAGE);
            EntityItem plant = new EntityItem(world, x + 0.5D, y + 0.8D, z + 0.5D, seed);
            plant.motionX = (rand.nextDouble() * 0.3D + 0.1D) * rand.nextInt(2) == 0 ? 1 : -1;
            plant.motionY = 0.1F;
            plant.motionZ = (rand.nextDouble() * 0.3D + 0.1D) * rand.nextInt(2) == 0 ? 1 : -1;
            plant.lifespan = 300;
            ItemPlasticPlants.markInactive(plant);
            world.spawnEntityInWorld(plant);
            plant.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
        }
    }
}
