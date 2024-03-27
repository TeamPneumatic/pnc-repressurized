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
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class ProgramDrill extends AssemblyProgram {

    @Override
    public boolean validateBlockEntity(AssemblyControllerBlockEntity.AssemblySystem system) {
        return system.getDrill() != null;
    }

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL};
    }

    @Override
    public boolean executeStep(AssemblyControllerBlockEntity.AssemblySystem system) {
        boolean useAir = true;

        Level world = system.getPlatform().getLevel();

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeDrilled(world, system.getPlatform().getHeldStack())) {
                system.getDrill().goDrilling();
            } else if (system.getDrill().isIdle()) {
                useAir = system.getExportUnit().pickupItem(null);
            }
        } else if (!system.getExportUnit().isIdle()) {
            useAir = system.getExportUnit().pickupItem(null);
        } else {
            useAir = system.getImportUnit().pickupItem(getRecipeList(world));
        }

        return useAir;
    }

    private boolean canItemBeDrilled(Level world, ItemStack item) {
        return ModRecipeTypes.ASSEMBLY_DRILL.get().findFirst(world, r -> r.matches(item)).isPresent();
    }

    @Override
    public void writeToNBT(CompoundTag tag) {

    }

    @Override
    public void readFromNBT(CompoundTag tag) {

    }

    @Override
    public Collection<AssemblyRecipe> getRecipeList(Level level) {
        return ModRecipeTypes.ASSEMBLY_DRILL.get().allRecipes(level);
    }

    @Override
    public AssemblyProgramItem getItem() {
        return ModItems.ASSEMBLY_PROGRAM_DRILL.get();
    }

}
