package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class BlockSquidPlant extends BlockPneumaticPlantBase{

    /** Maximum number of entities for limiting mob spawning */
    private final int MAX_NEARBY_ENTITIES = 6;

    /** Range for spawning new entities with mob spawners */
    private final int SPAWN_RANGE = 4;

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.SQUID_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_SQUID_PLANT_LOCATION;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public boolean canPlantGrowOnThisBlock(Block block, World world, int x, int y, int z){
        return block == Blocks.water || block == Blocks.flowing_water;
    }

    @Override
    public boolean skipGrowthCheck(World world, int x, int y, int z){
        return world.getBlockMetadata(x, y, z) == 14;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) {
            int nearbyEntityCount = world.getEntitiesWithinAABB(EntitySquid.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1).expand(SPAWN_RANGE * 2, 4.0D, SPAWN_RANGE * 2)).size();
            if(nearbyEntityCount < MAX_NEARBY_ENTITIES) {
                EntitySquid squid = new EntitySquid(world);
                double randXmotion = rand.nextDouble() - 0.5D;
                double randYmotion = 1D;// rand.nextDouble();
                double randZmotion = rand.nextDouble() - 0.5D;
                squid.setLocationAndAngles(x + 0.5D, y + 0.5D, z + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
                squid.motionX = randXmotion;
                squid.motionY = randYmotion;
                squid.motionZ = randZmotion;
                world.spawnEntityInWorld(squid);
                squid.spawnExplosionParticle();
                squid.playSound("mob.newsound.chickenplop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                world.setBlockMetadataWithNotify(x, y, z, 11, 3);
            }
        } else {
            world.setBlockMetadataWithNotify(x, y, z, 14, 2);
            world.scheduleBlockUpdate(x, y, z, this, 60);
        }
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand){
        if(world.getBlockMetadata(x, y, z) == 14) world.spawnParticle("splash", (double)x + 0.5F, (double)y + 0.5F, (double)z + 0.5F, 0.0F, 0.0F, 0.0F);
    }
}
