package pneumaticCraft.common;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemAssemblyProgram;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class VillagerHandler implements IVillageTradeHandler{
    private static final VillagerHandler INSTANCE = new VillagerHandler();

    public static VillagerHandler instance(){
        return INSTANCE;
    }

    public void init(){
        VillagerRegistry.instance().registerVillagerId(Config.villagerMechanicID);
        VillagerRegistry.instance().registerVillageTradeHandler(Config.villagerMechanicID, this);

        PneumaticCraft.proxy.registerVillagerSkins();
    }

    @Override
    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random rand){
        recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(net.minecraft.init.Items.emerald, 10 + rand.nextInt(10)), null, new ItemStack(Itemss.PCBBlueprint)));
        for(int i = 0; i < ItemAssemblyProgram.PROGRAMS_AMOUNT; i++) {
            recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(net.minecraft.init.Items.emerald, rand.nextInt(5) + 7), null, new ItemStack(Itemss.assemblyProgram, 1, i)));
        }
        recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(net.minecraft.init.Items.emerald, rand.nextInt(5) + 1), null, new ItemStack(Itemss.nukeVirus)));
        recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(net.minecraft.init.Items.emerald, rand.nextInt(5) + 1), null, new ItemStack(Itemss.stopWorm)));
    }
}
