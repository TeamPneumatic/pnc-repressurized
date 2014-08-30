package pneumaticCraft.common.thirdparty.hydraulicraft;

import java.util.ArrayList;

import k4unl.minecraft.Hydraulicraft.api.IHarvesterTrolley;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.Textures;

public class TrolleyPlasticPlants implements IHarvesterTrolley{

    @Override
    public String getName(){
        return "plasticPlants";
    }

    @Override
    public boolean canHarvest(World world, int x, int y, int z){
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 6 || meta == 13;
    }

    @Override
    public boolean canPlant(World world, int x, int y, int z, ItemStack seed){
        return ((BlockPneumaticPlantBase)ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage() % 16)).canBlockStay(world, x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getHandlingSeeds(){
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(items);
        int start = items.size();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(items);
        for(int i = start; i < items.size(); i++) {
            items.get(i).setItemDamage(items.get(i).getItemDamage() + 16);
        }
        return items;
    }

    @Override
    public Block getBlockForSeed(ItemStack seed){
        return ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage() % 16);
    }

    @Override
    public ResourceLocation getTexture(){
        return Textures.MODEL_PLASTIC_TROLLEY;
    }

    @Override
    public int getPlantHeight(World world, int x, int y, int z){
        return 1;
    }

}
