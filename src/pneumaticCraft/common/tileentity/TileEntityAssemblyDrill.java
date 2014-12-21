package pneumaticCraft.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.recipes.programs.AssemblyProgram;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.TileEntityConstants;

public class TileEntityAssemblyDrill extends TileEntityAssemblyRobot{
    @DescSynced
    public boolean isDrillOn;
    @DescSynced
    @LazySynced
    private float drillSpeed;
    public float drillRotation;
    public float oldDrillRotation;
    private int drillStep;

    @Override
    public void updateEntity(){
        oldDrillRotation = drillRotation;
        super.updateEntity();
        if(isDrillOn) {
            drillSpeed = Math.min(drillSpeed + TileEntityConstants.ASSEMBLY_DRILL_ACCELERATION * speed, TileEntityConstants.ASSEMBLY_DRILL_MAX_SPEED);
        } else {
            drillSpeed = Math.max(drillSpeed - TileEntityConstants.ASSEMBLY_DRILL_ACCELERATION, 0);
        }
        drillRotation += drillSpeed;
        while(drillRotation >= 360) {
            drillRotation -= 360;
        }

        if(!worldObj.isRemote && drillStep > 0) {
            ForgeDirection[] platformDirection = getPlatformDirection();
            if(platformDirection == null) drillStep = 1;
            switch(drillStep){
                case 1:
                    slowMode = false;
                    gotoHomePosition();
                    break;
                case 2:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 3:
                    isDrillOn = true;
                    break;
                case 4:
                    slowMode = true;
                    gotoNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 5:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
                    isDrillOn = false;
                    TileEntity te = getTileEntityForCurrentDirection();
                    if(te instanceof TileEntityAssemblyPlatform) {
                        TileEntityAssemblyPlatform platform = (TileEntityAssemblyPlatform)te;
                        platform.hasDrilledStack = true;
                        ItemStack output = getDrilledOutputForItem(platform.getHeldStack());
                        if(output != null) {
                            platform.setHeldStack(output);
                        }
                    }
                    break;
                case 6:
                    slowMode = false;
                    gotoHomePosition();
                    break;
            }
            if(isDoneInternal()) {
                drillStep++;
                if(drillStep > 6) drillStep = 0;
            }
        }

    }

    public void goDrilling(){
        if(drillStep == 0) {
            drillStep = 1;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("drill", isDrillOn);
        tag.setFloat("drillSpeed", drillSpeed);
        tag.setInteger("drillStep", drillStep);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        isDrillOn = tag.getBoolean("drill");
        drillSpeed = tag.getFloat("drillSpeed");
        drillStep = tag.getInteger("drillStep");
    }

    @Override
    public boolean isIdle(){
        return drillStep == 0 && isDoneInternal();
    }

    private boolean isDoneInternal(){
        if(super.isDoneMoving()) {
            return isDrillOn ? drillSpeed > TileEntityConstants.ASSEMBLY_DRILL_MAX_SPEED - 1F : PneumaticCraftUtils.areFloatsEqual(drillSpeed, 0F);
        } else {
            return false;
        }
    }

    @Override
    public boolean canMoveToDiagonalNeighbours(){
        return false;
    }

    public static ItemStack getDrilledOutputForItem(ItemStack input){
        for(AssemblyRecipe recipe : AssemblyRecipe.drillRecipes) {
            if(AssemblyProgram.isValidInput(recipe, input)) return recipe.getOutput().copy();
        }
        return null;
    }

    @Override
    public boolean reset(){
        if(isIdle()) return true;
        else {
            isDrillOn = false;
            drillStep = 6;
            return false;
        }
    }

}
