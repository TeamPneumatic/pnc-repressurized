package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketPlaySound;
import pneumaticCraft.lib.Textures;

public class BlockCreeperPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.CREEPER_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_CREEPER_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public boolean skipGrowthCheck(World world, int x, int y, int z){
        return world.getBlockMetadata(x, y, z) == 14;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random){
        return par1Random.nextInt(2) + 1;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) {
            if(!world.isRemote) {
                world.createExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, 0.5F, false);
                EntityItem item = new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.CREEPER_PLANT_DAMAGE));
                item.motionX = (rand.nextGaussian() - 0.5D) / 2;
                item.motionY = rand.nextDouble();
                item.motionZ = (rand.nextGaussian() - 0.5D) / 2;
                item.lifespan = 300;
                ItemPlasticPlants.markInactive(item);
                world.spawnEntityInWorld(item);
                world.setBlock(x, y, z, this, world.getBlockMetadata(x, y, z) - 2, 3);
            }
        } else {
            world.setBlockMetadataWithNotify(x, y, z, 14, 3);
            NetworkHandler.sendToAllAround(new PacketPlaySound("creeper.primed", x + 0.5D, y + 0.5D, z + 0.5D, 1.0F, 1.0F, true), world);
            world.scheduleBlockUpdate(x, y, z, this, 60);
        }
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) world.spawnParticle("largesmoke", x + 0.5D, y + 0.9D, z + 0.5D, 0, 0, 0);
    }
}
