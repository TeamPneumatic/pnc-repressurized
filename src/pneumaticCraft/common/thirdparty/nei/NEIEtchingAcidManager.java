package pneumaticCraft.common.thirdparty.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import codechicken.nei.PositionedStack;

public class NEIEtchingAcidManager extends NEISpecialCraftingManager{

    public NEIEtchingAcidManager(){
        setText("gui.nei.recipe.etchingAcid");
    }

    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Fluids.getBlock(Fluids.etchingAcid).getUnlocalizedName() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        MultipleInputOutputRecipe recipe = new MultipleInputOutputRecipe();
        recipe.addIngredient(new PositionedStack(new ItemStack(Itemss.emptyPCB), 41, 80));
        recipe.addIngredient(new PositionedStack(new ItemStack(Fluids.getBucket(Fluids.etchingAcid)), 73, 80));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.unassembledPCB), 105, 80));
        recipes.add(recipe);
        return recipes;
    }
}
