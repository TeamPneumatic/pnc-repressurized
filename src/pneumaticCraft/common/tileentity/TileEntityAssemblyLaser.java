package pneumaticCraft.common.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.recipe.AssemblyRecipe;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.recipes.programs.AssemblyProgram;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class TileEntityAssemblyLaser extends TileEntityAssemblyRobot{
    @DescSynced
    public boolean isLaserOn;
    private int laserStep;//used to progressively draw a circle.
    private static final float ITEM_SIZE = 10F;

    @Override
    public void updateEntity(){
        super.updateEntity();
        if(laserStep > 0) {
            ForgeDirection[] platformDirection = getPlatformDirection();
            if(platformDirection == null) {
                laserStep = 105;
            }
            switch(laserStep){
                case 1:
                    //                    isLaserOn = false;
                    slowMode = false;
                    //                    gotoHomePosition();
                    break;
                case 2:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 3:
                    slowMode = true;
                    gotoNeighbour(platformDirection[0], platformDirection[1]);
                    break;
                case 104:
                    hoverOverNeighbour(platformDirection[0], platformDirection[1]);
                    isLaserOn = false;
                    slowMode = true;
                    TileEntity te = getTileEntityForCurrentDirection();
                    if(te instanceof TileEntityAssemblyPlatform) {
                        TileEntityAssemblyPlatform platform = (TileEntityAssemblyPlatform)te;
                        platform.hasLaseredStack = true;
                        ItemStack output = getLaseredOutputForItem(platform.getHeldStack());
                        if(output != null) {
                            platform.setHeldStack(output);
                        }
                    }
                    break;
                case 105:
                    slowMode = false;
                    isLaserOn = false;
                    gotoHomePosition();
                    break;
                default: //4-103
                    isLaserOn = true;
                    slowMode = false;
                    targetAngles[EnumAngles.BASE.ordinal()] = 100F - (float)PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.circlePoints / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.MIDDLE.ordinal()] = -10F + (float)PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.circlePoints / 100] * ITEM_SIZE;
                    targetAngles[EnumAngles.TAIL.ordinal()] = 0F;
                    targetAngles[EnumAngles.TURN.ordinal()] += (float)PneumaticCraftUtils.sin[(laserStep - 4) * PneumaticCraftUtils.circlePoints / 100] * ITEM_SIZE * 0.03D;
                    break;
            }
            if(isDoneInternal() || laserStep >= 4 && laserStep <= 103) {
                laserStep++;
                if(laserStep > 105) laserStep = 0;
            }
        }
    }

    public void startLasering(){
        if(laserStep == 0) {
            laserStep = 1;
        }
    }

    @Override
    public boolean gotoNeighbour(ForgeDirection primaryDir, ForgeDirection secondaryDir){
        boolean diagonal = super.gotoNeighbour(primaryDir, secondaryDir);
        targetAngles[EnumAngles.TURN.ordinal()] -= ITEM_SIZE * 0.45D;
        return diagonal;
    }

    private boolean isDoneInternal(){
        return super.isDoneMoving();
    }

    @Override
    public boolean isIdle(){
        return laserStep == 0 && isDoneInternal();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("laser", isLaserOn);
        tag.setInteger("laserStep", laserStep);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        isLaserOn = tag.getBoolean("laser");
        laserStep = tag.getInteger("laserStep");
    }

    @Override
    public boolean canMoveToDiagonalNeighbours(){
        return false;
    }

    public static ItemStack getLaseredOutputForItem(ItemStack input){
        for(AssemblyRecipe recipe : AssemblyRecipe.laserRecipes) {
            if(AssemblyProgram.isValidInput(recipe, input)) return recipe.getOutput().copy();
        }
        return null;
    }

    @Override
    public boolean reset(){
        if(isIdle()) return true;
        else {
            isLaserOn = false;
            laserStep = 105;
            return false;
        }
    }
}
