package pneumaticCraft.common.block.pneumaticPlants;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.lib.Textures;

public class BlockEnderPlant extends BlockPneumaticPlantBase{

    @Override
    protected int getSeedDamage(){
        return ItemPlasticPlants.ENDER_PLANT_DAMAGE;
    }

    @Override
    protected String getTextureString(){
        return Textures.ICON_ENDER_PLANT_LOCATION;
    }

    @Override
    public boolean canPlantGrowOnThisBlock(Block blockID, World world, int x, int y, int z){
        return blockID == Blocks.end_stone;
    }

    @Override
    protected boolean canGrowWithLightValue(int lightValue){
        return lightValue >= 10;
    }

    @Override
    public void executeFullGrownEffect(World world, int x, int y, int z, Random rand){
        if(!world.isRemote) {
            for(int i = 0; i < 50; i++) {
                int randX = x + rand.nextInt(30) - 15;
                int randY = y + rand.nextInt(8);
                int randZ = z + rand.nextInt(30) - 15;
                Block block = world.getBlock(randX, randY, randZ);
                if(!block.getMaterial().blocksMovement()) {
                    ItemStack seed = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.ENDER_PLANT_DAMAGE);
                    EntityItem plant = new EntityItem(world, randX + 0.5D, randY + 0.5D, randZ + 0.5D, seed);
                    // plant.motionX = plant.motionY = plant.motionZ = 0;
                    plant.lifespan = 300;
                    ItemPlasticPlants.markInactive(plant);
                    world.spawnEntityInWorld(plant);
                    plant.playSound("mob.endermen.portal", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    short short1 = 128;
                    for(int j = 0; j < short1; ++j) {
                        double d6 = j / (short1 - 1.0D);
                        float f = (rand.nextFloat() - 0.5F) * 0.2F;
                        float f1 = (rand.nextFloat() - 0.5F) * 0.2F;
                        float f2 = (rand.nextFloat() - 0.5F) * 0.2F;
                        double d7 = x + 0.5D + (plant.posX - (x + 0.5D)) * d6 + (rand.nextDouble() - 0.5D);
                        double d8 = y + 0.5D + (plant.posY - (y + 0.5D)) * d6 + rand.nextDouble();
                        double d9 = z + 0.5D + (plant.posZ - (z + 0.5D)) * d6 + (rand.nextDouble() - 0.5D);
                        NetworkHandler.sendToAllAround(new PacketSpawnParticle("portal", d7, d8, d9, f, f1, f2), world);
                    }
                    world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) - 2, 3);
                    break;
                }
            }

        }
    }
}