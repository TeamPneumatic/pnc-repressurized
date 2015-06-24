package pneumaticCraft.common.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pneumaticCraft.common.item.Itemss;

public class RecipeGun implements IRecipe{
    private final String dyeName;
    private final Item output;

    public RecipeGun(String dyeName, Item output){
        this.dyeName = dyeName;
        this.output = output;
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world){
        ShapedOreRecipe recipe = new ShapedOreRecipe(new ItemStack(Itemss.pneumaticWrench, 1, Itemss.pneumaticWrench.getMaxDamage()), "idi", "c  ", "ili", 'd', dyeName, 'i', Itemss.ingotIronCompressed, 'l', Blocks.lever, 'c', new ItemStack(Itemss.airCanister, 1, OreDictionary.WILDCARD_VALUE));
        return recipe.matches(inventory, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory){
        if(!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            if(inventory.getStackInSlot(i) != null && inventory.getStackInSlot(i).getItem() == Itemss.airCanister) {
                output.setItemDamage(inventory.getStackInSlot(i).getItemDamage());
            }
        }
        return output;
    }

    @Override
    public int getRecipeSize(){
        return 3;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(output, 1, 0);
    }

}
