package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.lib.PneumaticValues;

public class RecipePneumaticHelmet implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting inventory, World world){

        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            if(i != 4 && i < 6) {
                if(inventory.getStackInSlot(i) == null) return false;
            } else {
                if(inventory.getStackInSlot(i) != null) return false;
            }
        }

        if(inventory.getStackInRowAndColumn(0, 0).getItem() != Itemss.airCanister) return false;
        // System.out.println("still ok");
        if(inventory.getStackInRowAndColumn(1, 0).getItem() != Itemss.printedCircuitBoard) return false;
        if(inventory.getStackInRowAndColumn(2, 0).getItem() != Itemss.airCanister) return false;
        if(inventory.getStackInRowAndColumn(0, 1).getItem() != Itemss.airCanister) return false;
        if(inventory.getStackInRowAndColumn(2, 1).getItem() != Itemss.airCanister) return false;
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory){
        if(!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        int totalDamage = inventory.getStackInRowAndColumn(0, 0).getItemDamage() + inventory.getStackInRowAndColumn(2, 0).getItemDamage() + inventory.getStackInRowAndColumn(0, 1).getItemDamage() + inventory.getStackInRowAndColumn(2, 1).getItemDamage();

        ((IPressurizable)output.getItem()).addAir(output, PneumaticValues.PNEUMATIC_HELMET_VOLUME * 10 - totalDamage);
        // output.setItemDamage(totalDamage);
        return output;
    }

    @Override
    public int getRecipeSize(){
        return 3;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(Itemss.pneumaticHelmet);
    }

}
