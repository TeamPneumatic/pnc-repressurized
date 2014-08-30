package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import pneumaticCraft.common.item.Itemss;

public class RecipeGun implements IRecipe{
    private final int plasticMeta;
    private final Item output;

    public RecipeGun(int plasticMeta, Item output){
        this.plasticMeta = plasticMeta;
        this.output = output;
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world){

        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            if(i != 4 && i != 5) {
                if(inventory.getStackInSlot(i) == null) return false;
            } else {
                if(inventory.getStackInSlot(i) != null) return false;
            }
        }
        ItemStack yellowPlastic = new ItemStack(Itemss.plastic, 1, plasticMeta);
        if(!inventory.getStackInRowAndColumn(0, 0).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(1, 0).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(2, 0).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(0, 1).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(0, 2).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(1, 2).isItemEqual(yellowPlastic)) return false;
        if(!inventory.getStackInRowAndColumn(2, 2).isItemEqual(yellowPlastic)) return false;
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory){
        if(!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        output.setItemDamage(inventory.getStackInRowAndColumn(0, 1).getItemDamage());
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
