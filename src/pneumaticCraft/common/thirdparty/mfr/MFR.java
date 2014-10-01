package pneumaticCraft.common.thirdparty.mfr;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.ModIds;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameData;

public class MFR implements IThirdParty{

    @Override
    public void preInit(){
        List<ItemStack> seeds = new ArrayList<ItemStack>();
        ((ItemPlasticPlants)Itemss.plasticPlant).addSubItems(seeds);
        for(ItemStack seed : seeds) {
            Block plantBlock = ItemPlasticPlants.getPlantBlockIDFromSeed(seed.getItemDamage());
            FMLInterModComms.sendMessage(ModIds.MFR, "registerHarvestable_Crop", new ItemStack(plantBlock, 1, 6));

            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("seed", GameData.getItemRegistry().getNameForObject(seed.getItem()));
            tag.setInteger("meta", seed.getItemDamage());
            tag.setString("crop", GameData.getBlockRegistry().getNameForObject(plantBlock));
            FMLInterModComms.sendMessage(ModIds.MFR, "registerPlantable_Standard", tag);
        }

    }

    @Override
    public void init(){

    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){

    }

}
