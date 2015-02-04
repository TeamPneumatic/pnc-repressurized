package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class BlockHeliumPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_HELIUM_PLANT_LOCATION;
    }

    @Override
    public boolean isPlantHanging(){
        return true;
    }

    @Override
    public boolean canPlantGrowOnThisBlock(Block block, World world, int x, int y, int z){
        return block == Blocks.netherrack;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random){
        return /* par1Random.nextInt(2) + */1;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote) {
            ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE);
            EntityItem plant = new EntityItem(world, x + 0.5D, y + 0.2D, z + 0.5D, seed);
            plant.motionX = rand.nextFloat() - 0.5F;
            plant.motionY = -1.0F;
            plant.motionZ = rand.nextFloat() - 0.5F;
            plant.lifespan = 300;
            ItemPlasticPlants.markInactive(plant);
            world.spawnEntityInWorld(plant);
            plant.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);

            world.setBlockMetadataWithNotify(x, y, z, 4, 3);
        }
    }
}
