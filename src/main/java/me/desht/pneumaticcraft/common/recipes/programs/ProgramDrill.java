package me.desht.pneumaticcraft.common.recipes.programs;

import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ProgramDrill extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController.AssemblySystem system) {
        boolean useAir = true;

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeDrilled(system.getPlatform().getHeldStack())) {
                system.getDrill().goDrilling();
            } else if (system.getDrill().isIdle()) {
                useAir = system.getExportUnit().pickupItem(null);
            }
        } else if (!system.getExportUnit().isIdle()) {
            useAir = system.getExportUnit().pickupItem(null);
        } else {
            useAir = system.getImportUnit().pickupItem(getRecipeList());
        }

        return useAir;
    }

    private boolean canItemBeDrilled(ItemStack item) {
        for (AssemblyRecipe recipe : getRecipeList()) {
            if (isValidInput(recipe, item)) return true;
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {

    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {

    }

    @Override
    public List<AssemblyRecipe> getRecipeList() {
        return AssemblyRecipe.drillRecipes;
    }

    @Override
    protected int getItemMeta() {
        return ItemAssemblyProgram.DRILL_DAMAGE;
    }

}
