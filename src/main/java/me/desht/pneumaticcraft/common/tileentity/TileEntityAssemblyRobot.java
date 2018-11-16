package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityAssemblyRobot extends TileEntityTickableBase implements IAssemblyMachine, IResettable {
    public final float[] oldAngles = new float[5];
    @DescSynced
    @LazySynced
    public final float[] angles = new float[5];
    @DescSynced
    final float[] targetAngles = new float[5];
    EnumFacing[] targetDirection = new EnumFacing[]{null, null};
    @DescSynced
    boolean slowMode; //used for the drill when drilling, the slowmode moves the arm 10x as slow as normal.
    @DescSynced
    protected float speed = 1.0F;
    private BlockPos controllerPos;

    protected enum EnumAngles {
        TURN, BASE, MIDDLE, TAIL, HEAD
    }

    public TileEntityAssemblyRobot() {
        gotoHomePosition();
        for (int i = 0; i < 5; i++) {
            angles[i] = targetAngles[i];
            oldAngles[i] = targetAngles[i];
        }
    }


    @Override
    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    @Override
    public void onNeighborBlockUpdate() {
        super.onNeighborBlockUpdate();
        invalidateSystem();
    }

    void invalidateSystem() {
        if (controllerPos != null) {
            TileEntity te = getWorld().getTileEntity(controllerPos);
            if (te instanceof TileEntityAssemblyController) {
                ((TileEntityAssemblyController) te).invalidateAssemblySystem();
            }
        }
    }

    @Override
    public void update() {
        super.update();
        //set the old angles to the last tick calculated angles (used in rendering)
        // while(isDone()) {
        // gotoNeighbour(ForgeDirection.SOUTH, ForgeDirection.EAST);
        //     if(!isDone()) break;
        //     gotoHomePosition();
        // 

        System.arraycopy(angles, 0, oldAngles, 0, 5);
        //move the arms and claw more to their destination
        for (int i = 0; i < 5; i++) {
            if (angles[i] > targetAngles[i]) {
                angles[i] = Math.max(angles[i] - TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            } else if (angles[i] < targetAngles[i]) {
                angles[i] = Math.min(angles[i] + TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            }
        }
    }

    public void gotoHomePosition() {
        targetAngles[EnumAngles.TURN.ordinal()] = 0F;
        targetAngles[EnumAngles.BASE.ordinal()] = 0F;
        targetAngles[EnumAngles.MIDDLE.ordinal()] = 55F;
        targetAngles[EnumAngles.TAIL.ordinal()] = 35F;
        targetAngles[EnumAngles.HEAD.ordinal()] = 0F;
    }

    boolean gotoTarget() {
        if (targetDirection == null) return false;

        this.gotoNeighbour(targetDirection[0], targetDirection[1]);
        return isDoneMoving();
    }

    public void gotoNeighbour(EnumFacing direction) {
        gotoNeighbour(direction, null);
    }

    /**
     * Goes to the neighbour in the given direction(s).
     *
     * @param primaryDir the first horizontal direction to move in
     * @param secondaryDir the second horizontal direction to move in (may be null)
     * @return true if the neighbour is diagonal to this arm
     */
    @SuppressWarnings("incomplete-switch")
    public boolean gotoNeighbour(EnumFacing primaryDir, EnumFacing secondaryDir) {
        targetDirection = new EnumFacing[]{primaryDir, secondaryDir};
        boolean diagonal = true;
        boolean diagonalAllowed = canMoveToDiagonalNeighbours();
        switch (primaryDir) {
            case SOUTH:
                if (secondaryDir == EnumFacing.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = -45F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = 40F;
                } else if (secondaryDir == EnumFacing.WEST && diagonalAllowed) {
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
                if (secondaryDir == EnumFacing.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.ordinal()] = -135F;
                    targetAngles[EnumAngles.HEAD.ordinal()] = -40F;
                } else if (secondaryDir == EnumFacing.WEST && diagonalAllowed) {
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
        if (diagonal) {
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

    boolean hoverOverTarget() {
        if (targetDirection == null) return false;

        return this.hoverOverNeighbour(targetDirection);
    }

    private boolean hoverOverNeighbour(EnumFacing[] directions) {
        hoverOverNeighbour(directions[0], directions[1]);
        return isDoneMoving();
    }

    void hoverOverNeighbour(EnumFacing primaryDir, EnumFacing secondaryDir) {
        boolean diagonal = gotoNeighbour(primaryDir, secondaryDir);
        if (diagonal) {
            targetAngles[EnumAngles.BASE.ordinal()] = 160F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -95F;
            targetAngles[EnumAngles.TAIL.ordinal()] = -10F;
        } else {
            targetAngles[EnumAngles.BASE.ordinal()] = 100F;
            targetAngles[EnumAngles.MIDDLE.ordinal()] = -20F;
            targetAngles[EnumAngles.TAIL.ordinal()] = 10F;
        }
    }

    TileEntity getTileEntityForCurrentDirection() {
        return getTileEntityForDirection(targetDirection[0], targetDirection[1]);
    }

    public TileEntity getTileEntityForDirection(EnumFacing[] directions) {
        return getTileEntityForDirection(directions[0], directions[1]);
    }

    private TileEntity getTileEntityForDirection(EnumFacing firstDir, EnumFacing secondDir) {
        BlockPos pos = getPos().offset(firstDir);
        return getWorld().getTileEntity(secondDir == null ? pos : pos.offset(secondDir));
    }

    boolean isDoneMoving() {
        for (int i = 0; i < 5; i++) {
            if (!PneumaticCraftUtils.areFloatsEqual(angles[i], targetAngles[i])) return false;
        }
        return true;
    }

    public boolean isDoneRotatingYaw() {
        return PneumaticCraftUtils.areFloatsEqual(angles[EnumAngles.TURN.ordinal()], targetAngles[EnumAngles.TURN.ordinal()]);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < 5; i++) {
            angles[i] = tag.getFloat("angle" + i);
            targetAngles[i] = tag.getFloat("targetAngle" + i);
        }
        slowMode = tag.getBoolean("slowMode");
        speed = tag.getFloat("speed");
        targetDirection[0] = tag.hasKey("targetDir1") ? EnumFacing.VALUES[tag.getInteger("targetDir1")] : null;
        targetDirection[1] = tag.hasKey("targetDir2") ? EnumFacing.VALUES[tag.getInteger("targetDir2")] : null;

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < 5; i++) {
            tag.setFloat("angle" + i, angles[i]);
            tag.setFloat("targetAngle" + i, targetAngles[i]);
        }
        tag.setBoolean("slowMode", slowMode);
        tag.setFloat("speed", speed);

        if (targetDirection != null) {
            if (targetDirection[0] != null) tag.setInteger("targetDir1", targetDirection[0].ordinal());

            if (targetDirection[1] != null) tag.setInteger("targetDir2", targetDirection[1].ordinal());
        }
        return tag;
    }

    public abstract boolean canMoveToDiagonalNeighbours();

    EnumFacing[] getPlatformDirection() {
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            if (getWorld().getTileEntity(getPos().offset(dir)) instanceof TileEntityAssemblyPlatform)
                return new EnumFacing[]{dir, null};
        }
        if (canMoveToDiagonalNeighbours()) {
            for (EnumFacing secDir : new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST}) {
                for (EnumFacing primDir : new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH}) {
                    if (getWorld().getTileEntity(getPos().offset(primDir).offset(secDir)) instanceof TileEntityAssemblyPlatform) {
                        return new EnumFacing[]{primDir, secDir};
                    }
                }
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX() - 1, getPos().getY() - 1, getPos().getZ() - 1, getPos().getX() + 2, getPos().getY() + 2, getPos().getZ() + 2);
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
