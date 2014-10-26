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

public class BlockFireFlower extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.FIRE_FLOWER_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_FIRE_FLOWER_LOCATION;
    }

    @Override
    public boolean canPlantGrowOnThisBlock(Block block, World world, int x, int y, int z){
        return block == Blocks.netherrack;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return true;
    }

    @Override
    public boolean skipGrowthCheck(World world, int x, int y, int z){
        return world.getBlockMetadata(x, y, z) == 14;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) {
            world.setBlockMetadataWithNotify(x, y, z, 11, 3);
            if(!world.isRemote) {
                ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.FIRE_FLOWER_DAMAGE);
                EntityItem plant = new EntityItem(world, x + 0.5D, y + 0.8D, z + 0.5D, seed);
                plant.motionX = (rand.nextFloat() - 0.5F) / 2;
                plant.motionY = 0.5F;
                plant.motionZ = (rand.nextFloat() - 0.5F) / 2;
                plant.lifespan = 300;
                ItemPlasticPlants.markInactive(plant);
                world.spawnEntityInWorld(plant);
                plant.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
        } else {
            world.setBlockMetadataWithNotify(x, y, z, 14, 2);
            world.scheduleBlockUpdate(x, y, z, this, 60);
        }
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) world.spawnParticle("lava", x + 0.5D, y + 0.9D, z + 0.5D, 0, 0, 0);
    }
}
