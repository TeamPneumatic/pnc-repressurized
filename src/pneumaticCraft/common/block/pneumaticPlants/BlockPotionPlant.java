package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.world.World;
import pneumaticCraft.common.entity.projectile.EntityPotionCloud;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class BlockPotionPlant extends BlockPneumaticPlantBase{

    private static final int SQUARE_RADIUS = 20;
    private static final int MAX_HEIGHT_DIFF = 3;

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.POTION_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_POTION_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    /**
     * Significantly (twice as much per 3 brewing stands) increase the growth rate when there brewing stands that are currently brewing around the plant.
     */
    @Override
    protected float getGrowthRate(World world, int x, int y, int z){
        int brewingBrewingStands = 0;
        for(int i = x - SQUARE_RADIUS; i <= x + SQUARE_RADIUS; i++) {
            for(int j = y - MAX_HEIGHT_DIFF; j <= y + MAX_HEIGHT_DIFF; j++) {
                for(int k = z - SQUARE_RADIUS; k <= z + SQUARE_RADIUS; k++) {
                    if(world.getBlock(i, j, k) == Blocks.brewing_stand && world.getTileEntity(i, j, k) instanceof TileEntityBrewingStand) {
                        TileEntityBrewingStand brewingStand = (TileEntityBrewingStand)world.getTileEntity(i, j, k);
                        if(brewingStand.getBrewTime() > 0) brewingBrewingStands++;
                    }
                }
            }
        }
        return super.getGrowthRate(world, x, y, z) * (1 + brewingBrewingStands * 0.333F);
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
        if(!world.isRemote) world.spawnEntityInWorld(new EntityPotionCloud(world, x + 0.5D, y + 0.5D, z + 0.5D));
    }
}
