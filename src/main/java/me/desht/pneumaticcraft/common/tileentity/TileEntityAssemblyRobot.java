package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityAssemblyRobot extends TileEntityTickableBase implements IAssemblyMachine, IResettable {
    public final float[] oldAngles = new float[EnumAngles.values().length];
    @DescSynced
    @LazySynced
    public final float[] angles = new float[EnumAngles.values().length];
    @DescSynced
    final float[] targetAngles = new float[EnumAngles.values().length];
    Direction[] targetDirection = new Direction[]{null, null};
    @DescSynced
    boolean slowMode; //used for the drill when drilling, the slowmode moves the arm 10x as slow as normal.
    @DescSynced
    protected float speed = 1.0F;
    private BlockPos controllerPos;

    TileEntityAssemblyRobot(TileEntityType type) {
        super(type);

        gotoHomePosition();
        System.arraycopy(targetAngles, 0, angles, 0, targetAngles.length);
        System.arraycopy(targetAngles, 0, oldAngles, 0, targetAngles.length);
    }


    @Override
    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
    }

    @Override
    public void onNeighborBlockUpdate(BlockPos fromPos) {
        super.onNeighborBlockUpdate(fromPos);
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
    public void tick() {
        super.tick();

        System.arraycopy(angles, 0, oldAngles, 0, angles.length);

        // move the arms and claw toward their destination
        for (int i = 0; i < angles.length; i++) {
            if (angles[i] > targetAngles[i]) {
                angles[i] = Math.max(angles[i] - TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            } else if (angles[i] < targetAngles[i]) {
                angles[i] = Math.min(angles[i] + TileEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            }
        }
    }

    public void gotoHomePosition() {
        for (EnumAngles angle: EnumAngles.values()) {
            targetAngles[angle.getIndex()] = angle.getHomeAngle();
        }
    }

    boolean gotoTarget() {
        if (targetDirection == null) return false;

        this.gotoNeighbour(targetDirection[0], targetDirection[1]);
        return isDoneMoving();
    }

    /**
     * Goes to the neighbour in the given direction(s).
     *
     * @param primaryDir the first horizontal direction to move in
     * @param secondaryDir the second horizontal direction to move in (may be null)
     * @return true if the neighbour is diagonal to this arm
     */
    public boolean gotoNeighbour(Direction primaryDir, Direction secondaryDir) {
        targetDirection = new Direction[]{primaryDir, secondaryDir};
        boolean diagonal = true;
        boolean diagonalAllowed = canMoveToDiagonalNeighbours();
        switch (primaryDir) {
            case SOUTH:
                if (secondaryDir == Direction.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = -45F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = 40F;
                } else if (secondaryDir == Direction.WEST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = 45F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = -40F;
                } else {
                    targetAngles[EnumAngles.TURN.getIndex()] = 0F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = 90F;
                    diagonal = false;
                }
                break;
            case EAST:
                targetAngles[EnumAngles.TURN.getIndex()] = -90F;
                targetAngles[EnumAngles.HEAD.getIndex()] = 0F;
                diagonal = false;
                break;
            case NORTH:
                if (secondaryDir == Direction.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = -135F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = -40F;
                } else if (secondaryDir == Direction.WEST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = 135F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = 40F;
                } else {
                    targetAngles[EnumAngles.TURN.getIndex()] = 180F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = 90F;
                    diagonal = false;
                }
                break;
            case WEST:
                targetAngles[EnumAngles.TURN.getIndex()] = 90F;
                targetAngles[EnumAngles.HEAD.getIndex()] = 0F;
                diagonal = false;
                break;
        }
        if (diagonal) {
            targetAngles[EnumAngles.BASE.getIndex()] = 160F;
            targetAngles[EnumAngles.MIDDLE.getIndex()] = -85F;
            targetAngles[EnumAngles.TAIL.getIndex()] = -20F;
        } else {
            targetAngles[EnumAngles.BASE.getIndex()] = 100F;
            targetAngles[EnumAngles.MIDDLE.getIndex()] = -10F;
            targetAngles[EnumAngles.TAIL.getIndex()] = 0F;
        }
        return diagonal;
    }

    boolean hoverOverTarget() {
        if (targetDirection == null) return false;

        return this.hoverOverNeighbour(targetDirection);
    }

    private boolean hoverOverNeighbour(Direction[] directions) {
        hoverOverNeighbour(directions[0], directions[1]);
        return isDoneMoving();
    }

    void hoverOverNeighbour(Direction primaryDir, Direction secondaryDir) {
        boolean diagonal = gotoNeighbour(primaryDir, secondaryDir);
        if (diagonal) {
            targetAngles[EnumAngles.BASE.getIndex()] = 160F;
            targetAngles[EnumAngles.MIDDLE.getIndex()] = -95F;
            targetAngles[EnumAngles.TAIL.getIndex()] = -10F;
        } else {
            targetAngles[EnumAngles.BASE.getIndex()] = 100F;
            targetAngles[EnumAngles.MIDDLE.getIndex()] = -20F;
            targetAngles[EnumAngles.TAIL.getIndex()] = 10F;
        }
    }

    TileEntity getTileEntityForCurrentDirection() {
        return getTileEntityForDirection(targetDirection[0], targetDirection[1]);
    }

    private TileEntity getTileEntityForDirection(Direction firstDir, Direction secondDir) {
        BlockPos pos = getPos().offset(firstDir);
        return getWorld().getTileEntity(secondDir == null ? pos : pos.offset(secondDir));
    }

    boolean isDoneMoving() {
        for (int i = 0; i < angles.length; i++) {
            if (!PneumaticCraftUtils.epsilonEquals(angles[i], targetAngles[i])) return false;
        }
        return true;
    }

    @Override
    public void read(BlockState state, CompoundNBT tag) {
        super.read(state, tag);

        for (int i = 0; i < 5; i++) {
            angles[i] = tag.getFloat("angle" + i);
            targetAngles[i] = tag.getFloat("targetAngle" + i);
        }
        slowMode = tag.getBoolean("slowMode");
        speed = tag.getFloat("speed");
        targetDirection[0] = tag.contains("targetDir1") ? DirectionUtil.VALUES[tag.getInt("targetDir1")] : null;
        targetDirection[1] = tag.contains("targetDir2") ? DirectionUtil.VALUES[tag.getInt("targetDir2")] : null;

    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        for (int i = 0; i < 5; i++) {
            tag.putFloat("angle" + i, angles[i]);
            tag.putFloat("targetAngle" + i, targetAngles[i]);
        }
        tag.putBoolean("slowMode", slowMode);
        tag.putFloat("speed", speed);

        if (targetDirection != null) {
            if (targetDirection[0] != null) tag.putInt("targetDir1", targetDirection[0].getIndex());
            if (targetDirection[1] != null) tag.putInt("targetDir2", targetDirection[1].getIndex());
        }
        return tag;
    }

    public abstract boolean canMoveToDiagonalNeighbours();

    Direction[] getPlatformDirection() {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            if (getWorld().getTileEntity(getPos().offset(dir)) instanceof TileEntityAssemblyPlatform)
                return new Direction[]{dir, null};
        }
        if (canMoveToDiagonalNeighbours()) {
            for (Direction secDir : new Direction[]{Direction.WEST, Direction.EAST}) {
                for (Direction primDir : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                    if (getWorld().getTileEntity(getPos().offset(primDir).offset(secDir)) instanceof TileEntityAssemblyPlatform) {
                        return new Direction[]{primDir, secDir};
                    }
                }
            }
        }
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().getX() - 1, getPos().getY() - 1, getPos().getZ() - 1, getPos().getX() + 2, getPos().getY() + 2, getPos().getZ() + 2);
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    protected enum EnumAngles {
        TURN(0),
        BASE(1),
        MIDDLE(2, 55f),
        TAIL(3, 35f),
        HEAD(4);

        private final int idx;
        private final float homeAngle;

        EnumAngles(int idx) {
            this(idx, 0f);
        }

        EnumAngles(int idx, float homeAngle) {
            this.idx = idx;
            this.homeAngle = homeAngle;
        }

        public int getIndex() {
            return idx;
        }

        public float getHomeAngle() {
            return homeAngle;
        }
    }
}
