package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipePneumaticHelmet extends AbstractRecipe {

    public RecipePneumaticHelmet() {
        super("pneumatic_helmet");
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (i != 4 && i < 6) {
                if (inventory.getStackInSlot(i).isEmpty()) return false;
            } else {
                if (!inventory.getStackInSlot(i).isEmpty()) return false;
            }
        }

        if (inventory.getStackInRowAndColumn(0, 0).getItem() != Itemss.AIR_CANISTER) return false;
        if (inventory.getStackInRowAndColumn(1, 0).getItem() != Itemss.PRINTED_CIRCUIT_BOARD) return false;
        if (inventory.getStackInRowAndColumn(2, 0).getItem() != Itemss.AIR_CANISTER) return false;
        if (inventory.getStackInRowAndColumn(0, 1).getItem() != Itemss.AIR_CANISTER) return false;
        return inventory.getStackInRowAndColumn(2, 1).getItem() == Itemss.AIR_CANISTER;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = getRecipeOutput();
        int totalDamage = inventory.getStackInRowAndColumn(0, 0).getItemDamage()
                + inventory.getStackInRowAndColumn(2, 0).getItemDamage()
                + inventory.getStackInRowAndColumn(0, 1).getItemDamage()
                + inventory.getStackInRowAndColumn(2, 1).getItemDamage();

        ((IPressurizable) output.getItem()).addAir(output, PneumaticValues.PNEUMATIC_HELMET_VOLUME * 10 - totalDamage);
        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }

//    @Override
//    public int getRecipeSize() {
//        return 3;
//    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.PNEUMATIC_HELMET);
    }

}
