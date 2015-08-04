package pneumaticCraft.common.thirdparty.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import codechicken.nei.PositionedStack;

public class NEIUVLightBoxManager extends NEISpecialCraftingManager{

    public NEIUVLightBoxManager(){
        setText("gui.nei.recipe.uvLightBox");
    }

    @Override
    public String getRecipeName(){
        return StatCollector.translateToLocal(Blockss.uvLightBox.getUnlocalizedName() + ".name");
    }

    @Override
    protected List<MultipleInputOutputRecipe> getAllRecipes(){
        List<MultipleInputOutputRecipe> recipes = new ArrayList<MultipleInputOutputRecipe>();
        MultipleInputOutputRecipe recipe = new MultipleInputOutputRecipe();
        recipe.addIngredient(new PositionedStack(new ItemStack(Itemss.emptyPCB, 1, Itemss.emptyPCB.getMaxDamage()), 41, 80));
        recipe.addIngredient(new PositionedStack(new ItemStack(Blockss.uvLightBox), 73, 80));
        recipe.addOutput(new PositionedStack(new ItemStack(Itemss.emptyPCB), 105, 80));
        recipes.add(recipe);
        return recipes;
    }
}
