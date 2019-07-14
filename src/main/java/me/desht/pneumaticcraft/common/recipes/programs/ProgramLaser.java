package me.desht.pneumaticcraft.common.recipes.programs;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

public class ProgramLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController.AssemblySystem system) {
        boolean useAir = true;

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeLasered(system.getPlatform().getHeldStack())) {
                system.getLaser().startLasering();
            } else {
                if (system.getLaser().isIdle()) {
                    useAir = system.getExportUnit().pickupItem(null);
                }
            }
        } else {
            if (!system.getExportUnit().isIdle()) {
                useAir = system.getExportUnit().pickupItem(null);
            } else {
                useAir = system.getImportUnit().pickupItem(getRecipeList());
            }
        }

        return useAir;
    }

    private boolean canItemBeLasered(ItemStack item) {
        for (AssemblyRecipe recipe : getRecipeList()) {
            if (isValidInput(recipe, item)) return true;
        }
        return false;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {

    }

    @Override
    public void readFromNBT(CompoundNBT tag) {

    }

    @Override
    public List<AssemblyRecipe> getRecipeList() {
        return AssemblyRecipe.laserRecipes;
    }

    @Override
    protected Item getItem() {
        return ModItems.ASSEMBLY_PROGRAM_LASER;
    }
}
