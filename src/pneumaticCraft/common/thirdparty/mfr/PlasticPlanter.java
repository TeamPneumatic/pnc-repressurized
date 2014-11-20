package pneumaticCraft.common.thirdparty.mfr;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
import powercrystals.minefactoryreloaded.api.ReplacementBlock;

public class PlasticPlanter implements IFactoryPlantable{

    @Override
    public Item getSeed(){
        return Itemss.plasticPlant;
    }

    @Override
    public boolean canBePlanted(ItemStack stack, boolean forFermenting){
        return true;
    }

    @Override
    public ReplacementBlock getPlantedBlock(World world, int x, int y, int z, ItemStack stack){
        return new ReplacementBlock(ItemPlasticPlants.getPlantBlockIDFromSeed(stack.getItemDamage() % 16));
    }

    @Override
    public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack){
        if(!world.isAirBlock(x, y, z)) return false;
        Block plant = ItemPlasticPlants.getPlantBlockIDFromSeed(stack.getItemDamage() % 16);
        return plant.canBlockStay(world, x, y, z);
    }

    @Override
    public void prePlant(World world, int x, int y, int z, ItemStack stack){}

    @Override
    public void postPlant(World world, int x, int y, int z, ItemStack stack){}

}
