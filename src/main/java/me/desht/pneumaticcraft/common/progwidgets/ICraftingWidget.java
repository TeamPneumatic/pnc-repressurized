package me.desht.pneumaticcraft.common.progwidgets;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.world.World;

import java.util.Optional;

public interface ICraftingWidget {
    CraftingInventory getCraftingGrid();

    Optional<ICraftingRecipe> getRecipe(World world, CraftingInventory grid);
}
