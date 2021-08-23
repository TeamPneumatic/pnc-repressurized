package me.desht.pneumaticcraft.common.recipes.assembly;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProgramDrillLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.DRILL, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController.AssemblySystem system) {
        boolean useAir = true;

        World world = system.getPlatform().getLevel();

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

    private boolean canItemBeLasered(World world, ItemStack item) {
        return PneumaticCraftRecipeType.ASSEMBLY_LASER.stream(world).anyMatch(recipe -> recipe.matches(item));
    }

    private boolean canItemBeDrilled(World world, ItemStack item) {
        return PneumaticCraftRecipeType.ASSEMBLY_DRILL.stream(world).anyMatch(recipe -> recipe.matches(item));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {

    }

    @Override
    public void readFromNBT(CompoundNBT tag) {

    }

    @Override
    public Collection<AssemblyRecipe> getRecipeList(World world) {
        return PneumaticCraftRecipeType.ASSEMBLY_DRILL_LASER.getRecipes(world).values();
    }

    @Override
    public ItemAssemblyProgram getItem() {
        return ModItems.ASSEMBLY_PROGRAM_DRILL_LASER.get();
    }
}
