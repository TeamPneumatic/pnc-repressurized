package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class BlockRainPlant extends BlockPneumaticPlantBase{
    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.RAIN_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_RAIN_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote && world.isRaining()) {
            ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.RAIN_PLANT_DAMAGE);
            EntityItem plant = new EntityItem(world, x + rand.nextInt(16) - 8, 128, z + rand.nextInt(16) - 8, seed);
            plant.lifespan = 300;
            ItemPlasticPlants.markInactive(plant);
            world.spawnEntityInWorld(plant);
            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
        }
    }
}
