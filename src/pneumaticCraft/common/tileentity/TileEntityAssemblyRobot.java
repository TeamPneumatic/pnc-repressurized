package pneumaticCraft.common.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.LazySynced;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileEntityAssemblyRobot extends TileEntityBase implements IAssemblyMachine, IResettable{
    public float[] oldAngles = new float[5];
    @DescSynced
    @LazySynced
    public float[] angles = new float[5];
    @DescSynced
    public float[] targetAngles = new float[5];
    public ForgeDirection[] targetDirection = new ForgeDirection[]{ForgeDirection.UNKNOWN, ForgeDirection.UNKNOWN};
    @DescSynced
    public boolean slowMode; //used for the drill when drilling, the slowmode moves the arm 10x as slow as normal.
    @DescSynced
    protected float speed = 1.0F;

    protected enum EnumAngles{
        TURN, BASE, MIDDLE, TAIL, HEAD
    }

    public TileEntityAssemblyRobot(){
        gotoHomePosition();
        for(int i = 0; i < 5; i++) {
            angles[i] = targetAngles[i];
            oldAngles[i] = targetAngles[i];
        }
    }

    @Override
    public void updateEntity(){
        super.updateEntity();
        //set the old angles to the last tick calculated angles (used in rendering)
        // while(isDone()) {
        // gotoNeighbour(ForgeDirection.SOUTH, ForgeDirection.EAST);
        //     if(!isDone()) break;
        //     gotoHomePosition();
        // 

        for(int i = 0; i < 5; i++) {
            oldAngles[i] = angles[i];
        }
        //move the arms and claw more to their destination
        for(int i = 0; i < 5; i++) {
            if(angles[i] > targetAngles[i]) {
                angles[i] = Math.max(angles[i] - TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            } else if(angles[i] < targetAngles[i]) {
                angles[i] = Math.min(angles[i] + TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            }
        }
    }

    public void gotoHomePosition(){
        targetAngles[EnumAngles.TURN.ordinal()] = 0F;
        targetAngles[EnumAngles.BASE.ordinal()] = 0F;
        targetAngles[EnumAngles.MIDDLE.ordinal()] = 55F;
        targetAngles[EnumAngles.TAIL.ordinal()] = 35F;
        targetAngles[EnumAngles.HEAD.ordinal()] = 0F;
    }

    public boolean gotoTarget(){
        if(targetDirection == null) return false;

        this.gotoNeighbour(targetDirection[0], targetDirection[1]);
        return isDoneMoving();
    }

    public void gotoNeighbour(ForgeDirection direction){
        gotoNeighbour(direction, ForgeDirection.UNKNOWN);
    }

    /**
     * Goes to the neighbour, and returns true if the neighbour was diagonal to this arm.
     * @param primaryDir
     * @param secondaryDir
     * @return
     */
    @SuppressWarnings("incomplete-switch")
    public boolean gotoNeighbour(ForgeDirection primaryDir, ForgeDirection secondaryDir){
        targetDirection = new ForgeDirection[]{primaryDir, secondaryDir};
        boolean diagonal = true;
        boolean diagonalAllowed = canMoveToDiagonalNeighbours();
        switch(primaryDir){
            case SOUTH:
                if(secondaryDir == ForgeDirection.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = -45F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = 40F;
                } else if(secondaryDir == ForgeDirection.WEST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = 45F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = -40F;
                } else {
                    targetAngles[EnumAngles.TURN.ordinal()] = 0F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = 90F;
                    diagonal = false;
                }
                break;
            case EAST:
                targetAngles[EnumAngles.TURN.ordinal()] = -90F;
                targetAngles[EnumAngles.HEAD.ordinal()] = 0F;
                diagonal = false;
                break;
            case NORTH:
                if(secondaryDir == ForgeDirection.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = -135F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = -40F;
                } else if(secondaryDir == ForgeDirection.WEST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = 135F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = 40F;
                } else {
                    targetAngles[EnumAngles.TURN.ordinal()] = 180F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = 90F;
                    diagonal = false;
                }
                break;
            case WEST:
                targetAngles[EnumAngles.TURN.ordinal()] = 90F;
                targetAngles[EnumAngles.HEAD.ordinal()] = 0F;
                diagonal = false;
                break;
        }
        if(diagonal) {
            targetAngles[EnumAngles.BASE.ordinal()] = 160F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -85F;
            targetAngles[EnumAngles.TAIL.ordinal()] = -20F;
        } else {
            targetAngles[EnumAngles.BASE.ordinal()] = 100F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -10F;
            targetAngles[EnumAngles.TAIL.ordinal()] = 0F;
        }
        return diagonal;
    }

    public boolean hoverOverTarget(){
        if(targetDirection == null) return false;

        return this.hoverOverNeighbour(targetDirection);
    }

    public boolean hoverOverNeighbour(ForgeDirection[] directions){
        hoverOverNeighbour(directions[0], directions[1]);
        return isDoneMoving();
    }

    public void hoverOverNeighbour(ForgeDirection primaryDir, ForgeDirection secondaryDir){
        boolean diagonal = gotoNeighbour(primaryDir, secondaryDir);
        if(diagonal) {
            targetAngles[EnumAngles.BASE.ordinal()] = 160F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -95F;
            targetAngles[EnumAngles.TAIL.ordinal()] = -10F;
        } else {
            targetAngles[EnumAngles.BASE.ordinal()] = 100F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -20F;
            targetAngles[EnumAngles.TAIL.ordinal()] = 10F;
        }
    }

    public TileEntity getTileEntityForCurrentDirection(){
        return getTileEntityForDirection(targetDirection[0], targetDirection[1]);
    }

    public TileEntity getTileEntityForDirection(ForgeDirection[] directions){
        return getTileEntityForDirection(directions[0], directions[1]);
    }

    public TileEntity getTileEntityForDirection(ForgeDirection firstDir, ForgeDirection secondDir){
        return worldObj.getTileEntity(xCoord + firstDir.offsetX + secondDir.offsetX, yCoord + firstDir.offsetY + secondDir.offsetY, zCoord + firstDir.offsetZ + secondDir.offsetZ);
    }

    protected boolean isDoneMoving(){
        for(int i = 0; i < 5; i++) {
            if(!PneumaticCraftUtils.areFloatsEqual(angles[i], targetAngles[i])) return false;
        }
        return true;
    }

    public boolean isDoneRotatingYaw(){
        return PneumaticCraftUtils.areFloatsEqual(angles[EnumAngles.TURN.ordinal()], targetAngles[EnumAngles.TURN.ordinal()]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        for(int i = 0; i < 5; i++) {
            angles[i] = tag.getFloat("angle" + i);
            targetAngles[i] = tag.getFloat("targetAngle" + i);
        }
        slowMode = tag.getBoolean("slowMode");
        speed = tag.getFloat("speed");
        targetDirection[0] = ForgeDirection.values()[tag.getInteger("targetDir1")];
        targetDirection[1] = ForgeDirection.values()[tag.getInteger("targetDir2")];

    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        for(int i = 0; i < 5; i++) {
            tag.setFloat("angle" + i, angles[i]);
            tag.setFloat("targetAngle" + i, targetAngles[i]);
        }
        tag.setBoolean("slowMode", slowMode);
        tag.setFloat("speed", speed);

        if(targetDirection != null) {
            if(targetDirection.length > 0) tag.setInteger("targetDir1", targetDirection[0].ordinal());

            if(targetDirection.length > 1) tag.setInteger("targetDir2", targetDirection[1].ordinal());
        }
    }

    public abstract boolean canMoveToDiagonalNeighbours();

    public ForgeDirection[] getPlatformDirection(){
        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if(dir != ForgeDirection.UP && dir != ForgeDirection.DOWN) {
                if(worldObj.getTileEntity(xCoord + dir.offsetX, yCoord, zCoord + dir.offsetZ) instanceof TileEntityAssemblyPlatform) return new ForgeDirection[]{dir, ForgeDirection.UNKNOWN};
            }
        }
        if(canMoveToDiagonalNeighbours()) {
            if(worldObj.getTileEntity(xCoord + ForgeDirection.NORTH.offsetX + ForgeDirection.WEST.offsetX, yCoord, zCoord + ForgeDirection.NORTH.offsetZ + ForgeDirection.WEST.offsetZ) instanceof TileEntityAssemblyPlatform) return new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.WEST};
            if(worldObj.getTileEntity(xCoord + ForgeDirection.NORTH.offsetX + ForgeDirection.EAST.offsetX, yCoord, zCoord + ForgeDirection.NORTH.offsetZ + ForgeDirection.EAST.offsetZ) instanceof TileEntityAssemblyPlatform) return new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.EAST};
            if(worldObj.getTileEntity(xCoord + ForgeDirection.SOUTH.offsetX + ForgeDirection.WEST.offsetX, yCoord, zCoord + ForgeDirection.SOUTH.offsetZ + ForgeDirection.WEST.offsetZ) instanceof TileEntityAssemblyPlatform) return new ForgeDirection[]{ForgeDirection.SOUTH, ForgeDirection.WEST};
            if(worldObj.getTileEntity(xCoord + ForgeDirection.SOUTH.offsetX + ForgeDirection.EAST.offsetX, yCoord, zCoord + ForgeDirection.SOUTH.offsetZ + ForgeDirection.EAST.offsetZ) instanceof TileEntityAssemblyPlatform) return new ForgeDirection[]{ForgeDirection.SOUTH, ForgeDirection.EAST};
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
        return AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord - 1, zCoord - 1, xCoord + 2, yCoord + 2, zCoord + 2);
    }

    @Override
    public void setSpeed(float speed){
        this.speed = speed;
    }

}
