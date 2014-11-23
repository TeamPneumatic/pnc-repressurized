package pneumaticCraft.common.thirdparty.mfr;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import powercrystals.minefactoryreloaded.api.HarvestType;
import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;

public class PlasticHarvester implements IFactoryHarvestable{

    private final Block plantBlock;

    public PlasticHarvester(Block plantBlock){
        this.plantBlock = plantBlock;
    }

    @Override
    public Block getPlant(){
        return plantBlock;
    }

    @Override
    public HarvestType getHarvestType(){
        return HarvestType.Normal;
    }

    @Override
    public boolean breakBlock(){
        return true;
    }

    @Override
    public boolean canBeHarvested(World world, Map<String, Boolean> harvesterSettings, int x, int y, int z){
        return world.getBlockMetadata(x, y, z) == 6 || world.getBlockMetadata(x, y, z) == 13;
    }

    @Override
    public List<ItemStack> getDrops(World world, Random rand, Map<String, Boolean> harvesterSettings, int x, int y, int z){
        return world.getBlock(x, y, z).getDrops(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
    }

    @Override
    public void preHarvest(World world, int x, int y, int z){}

    @Override
    public void postHarvest(World world, int x, int y, int z){}

}
