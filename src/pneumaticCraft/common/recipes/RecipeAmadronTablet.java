package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;

public class RecipeAmadronTablet implements IRecipe{
    @Override
    public boolean matches(InventoryCrafting inventory, World world){
        ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(Itemss.amadronTablet, 1, Itemss.amadronTablet.getMaxDamage()), "ppp", "pgp", "pcp", 'p', new ItemStack(Itemss.plastic, 1, ItemPlasticPlants.BURST_PLANT_DAMAGE), 'g', Itemss.GPSTool, 'c', new ItemStack(Itemss.airCanister, 1, OreDictionary.WILDCARD_VALUE));
        return recipe.matches(inventory, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory){
        if(!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        output.setItemDamage(inventory.getStackInRowAndColumn(1, 2).getItemDamage());
        return output;
    }

    @Override
    public int getRecipeSize(){
        return 3;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(Itemss.amadronTablet);
    }

}
