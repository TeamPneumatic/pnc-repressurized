package me.desht.pneumaticcraft.common.recipes.programs;

import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class ProgramDrill extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController controller, TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser) {
        boolean useAir = true;

        if (!platform.getHeldStack().isEmpty()) {
            if (canItemBeDrilled(platform.getHeldStack())) {
                drill.goDrilling();
            } else if (drill.isIdle()) {
                useAir = ioUnitExport.pickupItem(null);
            }
        } else if (!ioUnitExport.isIdle()) {
            useAir = ioUnitExport.pickupItem(null);
        } else {
            useAir = ioUnitImport.pickupItem(getRecipeList());
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

}
