package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import pneumaticCraft.common.item.Itemss;

public class RecipeManometer implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting inventory, World world){

        boolean gaugeFound = false;
        boolean canisterFound = false;
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(stack != null) {
                if(stack.getItem() == Itemss.pressureGauge) {
                    if(gaugeFound) return false;
                    gaugeFound = true;
                } else if(stack.getItem() == Itemss.airCanister) {
                    if(canisterFound) return false;
                    canisterFound = true;
                } else return false;
            }
        }
        return gaugeFound && canisterFound;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory){
        if(!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        output.setItemDamage(getCanister(inventory).getItemDamage());
        //System.out.println("output damage: " + output.getItemDamage());
        return output;
    }

    public ItemStack getCanister(InventoryCrafting inventory){
        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(stack != null && stack.getItem() == Itemss.airCanister) return stack;
        }
        return null;
    }

    @Override
    public int getRecipeSize(){
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(Itemss.manometer);
    }

}
