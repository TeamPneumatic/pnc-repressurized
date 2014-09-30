package pneumaticCraft.common.thirdparty.forestry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmable;

public class FarmablePlastic implements IFarmable{

    private final int meta;

    public FarmablePlastic(Block block){
        meta = getItemMetaForBlock(block);
    }

    public static int getItemMetaForBlock(Block block){
        List<ItemStack> seeds = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(seeds);
        for(ItemStack seed : seeds) {
            Block plantBlock = ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage());
            if(plantBlock == block) {
                return seed.getItemDamage();
            }
        }
        throw new IllegalArgumentException("No meta value found for the given block!");
    }

    public FarmablePlastic(int meta){

        this.meta = meta;
    }

    @Override
    public boolean isSaplingAt(World world, int x, int y, int z){
        Block block = world.getBlock(x, y, z);
        List<ItemStack> seeds = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(seeds);
        for(ItemStack seed : seeds) {
            if(seed.getItemDamage() % 16 == meta) {
                Block plantBlock = ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage());
                if(plantBlock == block) return true;
            }
        }
        return false;
    }

    @Override
    public ICrop getCropAt(World world, int x, int y, int z){
        if(!isSaplingAt(world, x, y, z)) return null;
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 6 || meta == 13 ? new ICrop(){

            private World world;
            private int x, y, z;

            public ICrop setCoord(World world, int x, int y, int z){
                this.world = world;
                this.x = x;
                this.y = y;
                this.z = z;
                return this;
            }

            @Override
            public Collection<ItemStack> harvest(){
                List<ItemStack> drops = world.getBlock(x, y, z).getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                for(ItemStack drop : drops) {
                    drop.setItemDamage(drop.getItemDamage() % 16);
                }
                world.func_147480_a(x, y, z, false);
                world.setBlockToAir(x, y, z);
                return drops;
            }

        }.setCoord(world, x, y, z) : null;
    }

    @Override
    public boolean isGermling(ItemStack itemstack){
        return itemstack.getItem() == Itemss.plasticPlant && itemstack.getItemDamage() % 16 == meta;
    }

    @Override
    public boolean isWindfall(ItemStack itemstack){
        return false;
    }

    @Override
    public boolean plantSaplingAt(EntityPlayer player, ItemStack germling, World world, int x, int y, int z){
        Block plant = ItemPlasticPlants.getPlantBlockIDFromSeed(germling.getItemDamage());
        if(plant.canBlockStay(world, x, y, z)) {
            return world.setBlock(x, y, z, plant);
        }
        return false;
    }

}
