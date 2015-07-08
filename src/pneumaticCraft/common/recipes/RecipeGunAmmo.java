package pneumaticCraft.common.recipes;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import pneumaticCraft.common.item.ItemGunAmmo;
import pneumaticCraft.common.item.Itemss;

public class RecipeGunAmmo implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting invCrafting, World world){
        int itemCount = 0;
        boolean foundPotion = false;
        boolean foundAmmo = false;
        for(int i = 0; i < invCrafting.getSizeInventory(); i++) {
            ItemStack stack = invCrafting.getStackInSlot(i);
            if(stack != null) {
                itemCount++;
                if(stack.getItem() == Items.potionitem) foundPotion = true;
                if(stack.getItem() == Itemss.gunAmmo) foundAmmo = true;
            }
        }
        return foundPotion && foundAmmo && itemCount == 2;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting invCrafting){
        ItemStack potion = null;
        ItemStack ammo = null;
        for(int i = 0; i < invCrafting.getSizeInventory(); i++) {
            ItemStack stack = invCrafting.getStackInSlot(i);
            if(stack != null) {
                if(stack.getItem() == Items.potionitem) {
                    potion = stack;
                } else {
                    ammo = stack;
                }
            }
        }
        ammo = ammo.copy();
        ItemGunAmmo.setPotion(ammo, potion);
        return ammo;
    }

    @Override
    public int getRecipeSize(){
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(Itemss.gunAmmo);
    }

}
