package me.desht.pneumaticcraft.common.recipes.assembly;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProgramDrillLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController.AssemblySystem system) {
        boolean useAir = true;

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeDrilled(system.getPlatform().getHeldStack())) {
                system.getDrill().goDrilling();
            } else if (system.getDrill().isIdle() && canItemBeLasered(system.getPlatform().getHeldStack())) {
                system.getLaser().startLasering();
            } else if (system.getDrill().isIdle() && system.getLaser().isIdle()) {
                useAir = system.getExportUnit().pickupItem(null);
            }
        } else if (!system.getExportUnit().isIdle()) {
            useAir = system.getExportUnit().pickupItem(null);
        } else {
            List<IAssemblyRecipe> recipes = new ArrayList<>();
            recipes.addAll(getRecipeList());
            recipes.addAll(new ProgramDrill().getRecipeList());
            recipes.addAll(new ProgramLaser().getRecipeList());
            useAir = system.getImportUnit().pickupItem(recipes);
        }

        return useAir;
    }

    private boolean canItemBeLasered(ItemStack item) {
        return PneumaticCraftRecipes.assemblyLaserRecipes.values().stream().anyMatch(recipe -> recipe.matches(item));
    }

    private boolean canItemBeDrilled(ItemStack item) {
        return PneumaticCraftRecipes.assemblyDrillRecipes.values().stream().anyMatch(recipe -> recipe.matches(item));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {

    }

    @Override
    public void readFromNBT(CompoundNBT tag) {

    }

    @Override
    public Collection<IAssemblyRecipe> getRecipeList() {
        return PneumaticCraftRecipes.assemblyLaserDrillRecipes.values();
    }

    @Override
    protected Item getItem() {
        return ModItems.ASSEMBLY_PROGRAM_DRILL_LASER;
    }
}
