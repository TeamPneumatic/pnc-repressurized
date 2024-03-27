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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProgramDrillLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(AssemblyControllerBlockEntity.AssemblySystem system) {
        boolean useAir = true;

        Level world = system.getPlatform().getLevel();

        if (!system.getPlatform().getHeldStack().isEmpty()) {
            if (canItemBeDrilled(world, system.getPlatform().getHeldStack())) {
                system.getDrill().goDrilling();
            } else if (system.getDrill().isIdle() && canItemBeLasered(world, system.getPlatform().getHeldStack())) {
                system.getLaser().startLasering();
            } else if (system.getDrill().isIdle() && system.getLaser().isIdle()) {
                useAir = system.getExportUnit().pickupItem(null);
            }
        } else if (!system.getExportUnit().isIdle()) {
            useAir = system.getExportUnit().pickupItem(null);
        } else {
            List<AssemblyRecipe> recipes = new ArrayList<>();
            recipes.addAll(getRecipeList(world));
            recipes.addAll(new ProgramDrill().getRecipeList(world));
            recipes.addAll(new ProgramLaser().getRecipeList(world));
            useAir = system.getImportUnit().pickupItem(recipes);
        }

        return useAir;
    }

    @Override
    public boolean validateBlockEntity(AssemblyControllerBlockEntity.AssemblySystem system) {
        return system.getDrill() != null && system.getLaser() != null;
    }

    private boolean canItemBeLasered(Level world, ItemStack item) {
        return ModRecipeTypes.ASSEMBLY_LASER.get().stream(world).anyMatch(recipe -> recipe.value().matches(item));
    }

    private boolean canItemBeDrilled(Level world, ItemStack item) {
        return ModRecipeTypes.ASSEMBLY_DRILL.get().stream(world).anyMatch(recipe -> recipe.value().matches(item));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {

    }

    @Override
    public void readFromNBT(CompoundTag tag) {

    }

    @Override
    public Collection<AssemblyRecipe> getRecipeList(Level level) {
        return ModRecipeTypes.ASSEMBLY_DRILL_LASER.get().allRecipes(level);
    }

    @Override
    public AssemblyProgramItem getItem() {
        return ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get();
    }
}
