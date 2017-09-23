package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipeManometer extends AbstractRecipe {

    public RecipeManometer() {
        super("manometer");
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {

        boolean gaugeFound = false;
        boolean canisterFound = false;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == Itemss.PRESSURE_GAUGE) {
                    if (gaugeFound) return false;
                    gaugeFound = true;
                } else if (stack.getItem() == Itemss.AIR_CANISTER) {
                    if (canisterFound) return false;
                    canisterFound = true;
                } else return false;
            }
        }
        return gaugeFound && canisterFound;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = getRecipeOutput();
        output.setItemDamage(getCanister(inventory).getItemDamage());
        //System.out.println("output damage: " + output.getItemDamage());
        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    private ItemStack getCanister(InventoryCrafting inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.getItem() == Itemss.AIR_CANISTER) return stack;
        }
        return null;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.MANOMETER);
    }

}
