package me.desht.pneumaticcraft.common.recipes.assembly;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemAssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.Collection;

public class ProgramLaser extends AssemblyProgram {

    @Override
    public EnumMachine[] getRequiredMachines() {
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController.AssemblySystem system) {
        boolean useAir = true;

        World world = system.getPlatform().getLevel();

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

    private boolean canItemBeLasered(World world, ItemStack item) {
        return PneumaticCraftRecipeType.ASSEMBLY_LASER.findFirst(world, r -> r.matches(item)) != null;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
    }

    @Override
    public Collection<AssemblyRecipe> getRecipeList(World world) {
        return PneumaticCraftRecipeType.ASSEMBLY_LASER.getRecipes(world).values();
    }

    @Override
    public ItemAssemblyProgram getItem() {
        return ModItems.ASSEMBLY_PROGRAM_LASER.get();
    }
}
