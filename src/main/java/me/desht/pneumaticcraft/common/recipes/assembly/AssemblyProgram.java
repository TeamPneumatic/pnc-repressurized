/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.recipes.assembly;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public abstract class AssemblyProgram {
    public enum EnumAssemblyProblem {
        NO_PROBLEM, NO_INPUT, NO_OUTPUT
    }

    public enum EnumMachine implements ITranslatableEnum {
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

        @Override
        public String getTranslationKey() {
            return "block.pneumaticcraft.assembly_" + this.toString().toLowerCase(Locale.ROOT);
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
    public void addProgramProblem(List<ITextComponent> problemList) {
        switch (curProblem) {
            case NO_INPUT:
                problemList.add(new StringTextComponent(TextFormatting.GRAY + "The input IO Unit can't find an inventory with a Block of Compressed Iron."));
                problemList.add(new StringTextComponent(TextFormatting.BLACK + "Place an inventory with a Block of Compressed Iron surrounding the IO Unit."));
                break;
            case NO_OUTPUT:
                problemList.add(new StringTextComponent(TextFormatting.GRAY + "The output IO Unit can't find an inventory to place the output in."));
                problemList.add(new StringTextComponent(TextFormatting.BLACK + "Place an inventory / make space in a connected inventory."));
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
