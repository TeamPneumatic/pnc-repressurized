package me.desht.pneumaticcraft.common.recipes.assembly;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;
import java.util.List;

public abstract class AssemblyProgram {
    public enum EnumAssemblyProblem {
        NO_PROBLEM, NO_INPUT, NO_OUTPUT
    }

    public enum EnumMachine {
        PLATFORM(ModBlocks.ASSEMBLY_PLATFORM),
        DRILL(ModBlocks.ASSEMBLY_DRILL),
        LASER(ModBlocks.ASSEMBLY_LASER),
        IO_UNIT_EXPORT(ModBlocks.ASSEMBLY_IO_UNIT_EXPORT),
        IO_UNIT_IMPORT(ModBlocks.ASSEMBLY_IO_UNIT_IMPORT),
        CONTROLLER(ModBlocks.ASSEMBLY_CONTROLLER);

        private final RegistryObject<? extends Block> blockSupplier;

        EnumMachine(RegistryObject<? extends Block> blockSupplier) {
            this.blockSupplier = blockSupplier;
        }

        public String getTranslationKey() {
            return "block.pneumaticcraft.assembly_" + this.toString().toLowerCase();
        }

        public Block getMachineBlock() {
            //noinspection unchecked
            return blockSupplier.get();
        }
    }

    public EnumAssemblyProblem curProblem = EnumAssemblyProblem.NO_PROBLEM;

    public AssemblyRecipe.AssemblyProgramType getType() {
        return getItem().getProgramType();
    }

    /**
     * Retrieves the needed machines for this Assembly Program. As a Controller is always needed this doesn't have to be returned.
     *
     * @return an array of machine types
     */
    public abstract EnumMachine[] getRequiredMachines();

    /**
     * Executes the given step of the assembly program. It is given all the machines it is allowed to control.
     * It's possible for the given machines to be null, but only if they aren't in the getRequiredMachines list.
     *
     * @return true if the controller should use air and display 'running'. Return false to display 'standby'.
     */
    public abstract boolean executeStep(TileEntityAssemblyController.AssemblySystem system);

    public abstract void writeToNBT(CompoundNBT tag);

    public abstract void readFromNBT(CompoundNBT tag);

    public abstract Collection<AssemblyRecipe> getRecipeList(World world);

    /**
     * You can add problem messages here if an assembly program has a problem with a certain step.
     *
     * @param problemList list to add to
     */
    @SuppressWarnings("incomplete-switch")
    public void addProgramProblem(List<String> problemList) {
        switch (curProblem) {
            case NO_INPUT:
                problemList.addAll(PneumaticCraftUtils.splitString(TextFormatting.GRAY + "The input IO Unit can't find an inventory with a Block of Compressed Iron.", GuiConstants.MAX_CHAR_PER_LINE));
                problemList.addAll(PneumaticCraftUtils.splitString(TextFormatting.BLACK + "Place an inventory with a Block of Compressed Iron surrounding the IO Unit.", GuiConstants.MAX_CHAR_PER_LINE));
                break;
            case NO_OUTPUT:
                problemList.addAll(PneumaticCraftUtils.splitString(TextFormatting.GRAY + "The output IO Unit can't find an inventory to place the output in.", GuiConstants.MAX_CHAR_PER_LINE));
                problemList.addAll(PneumaticCraftUtils.splitString(TextFormatting.BLACK + "Place an inventory / make space in a connected inventory.", GuiConstants.MAX_CHAR_PER_LINE));
                break;
        }
    }

    static boolean isValidInput(AssemblyRecipe recipe, ItemStack input) {
        return recipe.matches(input);
    }

    public static AssemblyProgram fromRecipe(AssemblyRecipe recipe) {
        return ItemAssemblyProgram.fromProgramType(recipe.getProgramType()).getProgram();
    }

    public abstract ItemAssemblyProgram getItem();
}
