package pneumaticCraft.common.recipes.programs;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.tileentity.TileEntityAssemblyController;
import pneumaticCraft.common.tileentity.TileEntityAssemblyDrill;
import pneumaticCraft.common.tileentity.TileEntityAssemblyIOUnit;
import pneumaticCraft.common.tileentity.TileEntityAssemblyLaser;
import pneumaticCraft.common.tileentity.TileEntityAssemblyPlatform;

public class ProgramLaser extends AssemblyProgram{

    @Override
    public EnumMachine[] getRequiredMachines(){
        return new EnumMachine[]{EnumMachine.PLATFORM, EnumMachine.IO_UNIT_EXPORT, EnumMachine.IO_UNIT_IMPORT, EnumMachine.LASER};
    }

    @Override
    public boolean executeStep(TileEntityAssemblyController controller, TileEntityAssemblyPlatform platform, TileEntityAssemblyIOUnit ioUnitImport, TileEntityAssemblyIOUnit ioUnitExport, TileEntityAssemblyDrill drill, TileEntityAssemblyLaser laser){
        if(ioUnitExport.inventory[0] != null) {
            ioUnitExport.exportHeldItem();
        } else {
            if(platform.hasLaseredStack) {
                ioUnitExport.pickUpPlatformItem();
            } else if(platform.getHeldStack() != null) {
                if(canItemBeLasered(platform.getHeldStack())) {
                    laser.startLasering();
                } else {
                    controller.resetSetup();
                }
            } else {
                //return ioUnitImport.pickUpInventoryItem(getRecipeList());
            	return(ioUnitImport.pickupItem(getRecipeList()));
            }
        }
        return true;
    }

    private boolean canItemBeLasered(ItemStack item){
        for(AssemblyRecipe recipe : getRecipeList()) {
            if(isValidInput(recipe, item)) return true;
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){

    }

    @Override
    public void readFromNBT(NBTTagCompound tag){

    }

    @Override
    public List<AssemblyRecipe> getRecipeList(){
        return AssemblyRecipe.laserRecipes;
    }

}
