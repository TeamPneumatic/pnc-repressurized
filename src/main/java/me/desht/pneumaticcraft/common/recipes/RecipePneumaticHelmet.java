package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipePneumaticHelmet extends AbstractRecipe {

    public RecipePneumaticHelmet() {
        super("pneumatic_helmet_actual");
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        //As the recipe is 2 high it could be in the 2nd row too.
        int offsetY = inventory.getStackInRowAndColumn(0, 0).isEmpty() ? 1 : 0;
        int slotOffset = offsetY == 1 ? 3 : 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (i < slotOffset || i == 4 + slotOffset || i >= 6 + slotOffset) {
                if (!inventory.getStackInSlot(i).isEmpty()) return false;
            } else {
                if (inventory.getStackInSlot(i).isEmpty()) return false;
            }
        }

        if (inventory.getStackInRowAndColumn(0, offsetY).getItem() != Itemss.AIR_CANISTER) return false;
        if (inventory.getStackInRowAndColumn(1, offsetY).getItem() != Itemss.PRINTED_CIRCUIT_BOARD) return false;
        if (inventory.getStackInRowAndColumn(2, offsetY).getItem() != Itemss.AIR_CANISTER) return false;
        if (inventory.getStackInRowAndColumn(0, offsetY + 1).getItem() != Itemss.AIR_CANISTER) return false;
        return inventory.getStackInRowAndColumn(2, offsetY + 1).getItem() == Itemss.AIR_CANISTER;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = getRecipeOutput();
      //As the recipe is 2 high it could be in the 2nd row too.
        int offsetY = inventory.getStackInRowAndColumn(0, 0).isEmpty() ? 1 : 0;
        int totalDamage = inventory.getStackInRowAndColumn(0, offsetY).getItemDamage()
                + inventory.getStackInRowAndColumn(2, offsetY).getItemDamage()
                + inventory.getStackInRowAndColumn(0, offsetY + 1).getItemDamage()
                + inventory.getStackInRowAndColumn(2, offsetY + 1).getItemDamage();

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
