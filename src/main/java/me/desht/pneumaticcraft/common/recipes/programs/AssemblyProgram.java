package me.desht.pneumaticcraft.common.recipes.programs;

import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.tileentity.*;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public abstract class AssemblyProgram {
    public enum EnumTubeProblem {
        NO_PROBLEM, NO_INPUT, NO_OUTPUT
    }

    public EnumTubeProblem curProblem = EnumTubeProblem.NO_PROBLEM;

    public enum EnumMachine {
        PLATFORM, DRILL, LASER, IO_UNIT_EXPORT, IO_UNIT_IMPORT
    }

    /**
     * Retrieves the needed machines for this Assembly Program. As a Controller is always needed this doesn't have to be returned.
     *
     * @return
     */
    public abstract EnumMachine[] getRequiredMachines();

    /**
     * Executes the given step of the assembly program. It is given all the machines it is allowed to control.
     * It's possible for the given machines to be null, but only if they aren't in the getRequiredMachines list.
     *
     * @return true if the controller should use air and display 'running'. Return false to display 'standby'.
     */
    public abstract boolean executeStep(TileEntityAssemblyController controller, TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser);

    public abstract void writeToNBT(NBTTagCompound tag);

    public abstract void readFromNBT(NBTTagCompound tag);

    public abstract List<AssemblyRecipe> getRecipeList();

    /**
     * You can add problem messages here if an assembly program has a problem with a certain step.
     *
     * @param problemList
     */
    @SuppressWarnings("incomplete-switch")
    public void addProgramProblem(List<String> problemList) {
        switch (curProblem) {
            case NO_INPUT:
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "The input IO Unit can't find an inventory with a Block of Compressed Iron.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.BLACK + "Place an inventory with a Block of Compressed Iron surrounding the IO Unit.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                break;
            case NO_OUTPUT:
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "The output IO Unit can't find an inventory to place the output in.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                problemList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.BLACK + "Place an inventory / make space in a connected inventory.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
                break;
        }
    }

    public static boolean isValidInput(AssemblyRecipe recipe, ItemStack input) {
        return !input.isEmpty() && (input.isItemEqual(recipe.getInput()) || PneumaticCraftUtils.isSameOreDictStack(input, recipe.getInput())) && input.getCount() == recipe.getInput().getCount();
    }
}
