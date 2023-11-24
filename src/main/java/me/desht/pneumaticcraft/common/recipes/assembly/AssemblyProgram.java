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
import me.desht.pneumaticcraft.common.block.entity.AssemblyControllerBlockEntity;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.util.ITranslatableEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.List;

public abstract class AssemblyProgram {

    public enum EnumAssemblyProblem {
        NO_PROBLEM, NO_INPUT, NO_OUTPUT;
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

        public Block getMachineBlock() {
            //noinspection unchecked
            return blockSupplier.get();
        }

        @Override
        public String getTranslationKey() {
            return getMachineBlock().getDescriptionId();
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
    public abstract boolean executeStep(AssemblyControllerBlockEntity.AssemblySystem system);

    public abstract boolean validateBlockEntity(AssemblyControllerBlockEntity.AssemblySystem system);

    public abstract void writeToNBT(CompoundTag tag);

    public abstract void readFromNBT(CompoundTag tag);

    public abstract Collection<AssemblyRecipe> getRecipeList(Level world);

    /**
     * You can add problem messages here if an assembly program has a problem with a certain step.
     *
     * @param problemList list to add to
     */
    @SuppressWarnings("incomplete-switch")
    public void addProgramProblem(List<Component> problemList) {
        switch (curProblem) {
            case NO_INPUT -> {
                problemList.add(Component.literal(ChatFormatting.GRAY + "The input IO Unit can't find an inventory with a Block of Compressed Iron."));
                problemList.add(Component.literal(ChatFormatting.BLACK + "Place an inventory with a Block of Compressed Iron surrounding the IO Unit."));
            }
            case NO_OUTPUT -> {
                problemList.add(Component.literal(ChatFormatting.GRAY + "The output IO Unit can't find an inventory to place the output in."));
                problemList.add(Component.literal(ChatFormatting.BLACK + "Place an inventory / make space in a connected inventory."));
            }
        }
    }

    static boolean isValidInput(AssemblyRecipe recipe, ItemStack input) {
        return recipe.matches(input);
    }

    public static AssemblyProgram fromRecipe(AssemblyRecipe recipe) {
        return AssemblyProgramItem.fromProgramType(recipe.getProgramType()).getProgram();
    }

    public abstract AssemblyProgramItem getItem();
}
