package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class BlockLightningPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_LIGHTNING_PLANT_LOCATION;
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
            int j = MathHelper.floor_double(x) + rand.nextInt(20) - 10;
            int k = MathHelper.floor_double(z) + rand.nextInt(20) - 10;
            int l = world.getPrecipitationHeight(j, k);
            if(world.canLightningStrikeAt(j, l, k)) {
                EntityLightningBolt lightning = new EntityLightningBolt(world, j, l, k);
                world.addWeatherEffect(lightning);
                world.setBlockMetadataWithNotify(x, y, z, 11, 3);
            }
        }
    }
}
