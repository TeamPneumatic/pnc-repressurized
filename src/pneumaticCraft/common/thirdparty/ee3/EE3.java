package pneumaticCraft.common.thirdparty.ee3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.api.recipe.PressureChamberRecipe;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.Log;

import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;

public class EE3 implements IThirdParty{

    @Override
    public void preInit(){
        for(ItemStack seed : ItemPlasticPlants.getBlockToSeedMap().values()) {
            EnergyValueRegistryProxy.addPreAssignedEnergyValue(seed, 16);
        }
    }

    @Override
    public void init(){

    }

    @Override
    public void postInit(){
        for(PressureChamberRecipe recipe : PressureChamberRecipe.chamberRecipes) {
            if(recipe.output.length == 1) {
                List<ItemStack> stacks = new ArrayList<ItemStack>();
                for(Object o : recipe.input) {
                    stacks.add(PneumaticRecipeRegistry.getSingleStack(o));
                }
                RecipeRegistryProxy.addRecipe(recipe.output[0], stacks);
            } else {
                Log.info("Found a Pressure Chamber recipe that has more than one output. This will cause problems with DynEMC!");
            }
        }
        registerAssemblyRecipes(AssemblyRecipe.drillLaserRecipes);
        registerAssemblyRecipes(AssemblyRecipe.drillRecipes);
        registerAssemblyRecipes(AssemblyRecipe.laserRecipes);
    }

    private void registerAssemblyRecipes(List<AssemblyRecipe> recipes){
        for(AssemblyRecipe recipe : recipes) {
            RecipeRegistryProxy.addRecipe(recipe.getOutput(), Arrays.asList(new ItemStack[]{recipe.getInput()}));
        }
    }

    @Override
    public void clientSide(){

    }

    @Override
    public void clientInit(){

    }

}
