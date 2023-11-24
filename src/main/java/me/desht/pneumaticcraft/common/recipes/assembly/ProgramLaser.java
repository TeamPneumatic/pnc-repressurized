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
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipeTypes;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class ProgramLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(AssemblyControllerBlockEntity.AssemblySystem system) {
        boolean useAir = true;

        Level world = system.getPlatform().getLevel();

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeLasered(world, system.getPlatform().getHeldStack())) {
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
                useAir = system.getImportUnit().pickupItem(getRecipeList(world));
            }
        }

        return useAir;
    }

    @Override
    public boolean validateBlockEntity(AssemblyControllerBlockEntity.AssemblySystem system) {
        return system.getLaser() != null;
    }

    private boolean canItemBeLasered(Level world, ItemStack item) {
        return ModRecipeTypes.ASSEMBLY_LASER.get().findFirst(world, r -> r.matches(item)) != null;
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
    }

    @Override
    public Collection<AssemblyRecipe> getRecipeList(Level world) {
        return ModRecipeTypes.getRecipes(world, ModRecipeTypes.ASSEMBLY_LASER);
    }

    @Override
    public AssemblyProgramItem getItem() {
        return ModItems.ASSEMBLY_PROGRAM_LASER.get();
    }
}
