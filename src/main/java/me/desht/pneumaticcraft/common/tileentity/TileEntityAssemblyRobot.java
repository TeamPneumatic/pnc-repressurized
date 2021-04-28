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

import javax.annotation.Nonnull;

public abstract class TileEntityAssemblyRobot extends TileEntityTickableBase implements IAssemblyMachine, IResettable {
    public final float[] oldAngles = new float[EnumAngles.values().length];
    @DescSynced
    @LazySynced
    public final float[] angles = new float[EnumAngles.values().length];
    @DescSynced
    final float[] targetAngles = new float[EnumAngles.values().length];
    TargetDirections targetDirection = null;
    @DescSynced
    boolean slowMode; // fine-adjustment mode: robot arm moves 10x slower than normal
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

        if (controllerPos != null) {
            PneumaticCraftUtils.getTileEntityAt(getWorld(), controllerPos, TileEntityAssemblyController.class)
                    .ifPresent(TileEntityAssemblyController::invalidateAssemblySystem);
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
        if (targetDirection != null) {
            this.gotoNeighbour(targetDirection);
            return isDoneMoving();
        } else {
            return false;
        }
    }

    boolean hoverOverTarget() {
        if (targetDirection != null) {
            hoverOverNeighbour(targetDirection);
            return isDoneMoving();
        } else {
            return false;
        }
    }

    /**
     * Instruct the robot arm to start moving to the neighbour in the given direction(s).
     *
     * @param newDirections the direction(s) to move in
     * @return true if the neighbour is diagonal to this arm
     */
    public boolean gotoNeighbour(@Nonnull TargetDirections newDirections) {
        targetDirection = newDirections;
        boolean diagonal = true;
        boolean diagonalAllowed = canMoveToDiagonalNeighbours();
        switch (targetDirection.primary) {
            case SOUTH:
                if (targetDirection.secondary == Direction.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = -45F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = 40F;
                } else if (targetDirection.secondary == Direction.WEST && diagonalAllowed) {
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
                if (targetDirection.secondary == Direction.EAST && diagonalAllowed) {
                    targetAngles[EnumAngles.TURN.getIndex()] = -135F;
                    targetAngles[EnumAngles.HEAD.getIndex()] = -40F;
                } else if (targetDirection.secondary == Direction.WEST && diagonalAllowed) {
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

    /**
     * Like gotoNeighbour(), but hovers higher above the block.
     *
     * @param directions the direction(s) to move in
     */
    void hoverOverNeighbour(TargetDirections directions) {
        boolean diagonal = gotoNeighbour(directions);
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
        if (targetDirection == null) {
            return null;
        } else if (targetDirection.secondary == null) {
            return getCachedNeighbor(targetDirection.primary);
        } else {
            return getWorld().getTileEntity(targetDirection.offset(getPosition()));
        }
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
        targetDirection = TargetDirections.readNBT(tag);

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
        if (targetDirection != null) targetDirection.writeNBT(tag);
        return tag;
    }

    protected abstract boolean canMoveToDiagonalNeighbours();

    TargetDirections getPlatformDirection() {
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            if (getWorld().getTileEntity(getPos().offset(dir)) instanceof TileEntityAssemblyPlatform)
                return new TargetDirections(dir);
        }
        if (canMoveToDiagonalNeighbours()) {
            for (Direction secDir : new Direction[]{Direction.WEST, Direction.EAST}) {
                for (Direction primDir : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                    if (getWorld().getTileEntity(getPos().offset(primDir).offset(secDir)) instanceof TileEntityAssemblyPlatform) {
                        return new TargetDirections(primDir, secDir);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(
                getPos().getX() - 1, getPos().getY() - 1, getPos().getZ() - 1,
                getPos().getX() + 2, getPos().getY() + 2, getPos().getZ() + 2
        );
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    enum EnumAngles {
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

    /**
     * A compound direction relative to the tile entity in question. First direction is always non-null, second
     * direction may be null for an adjacent neighbour, or non-null for a diagonal neighbour.
     */
    static class TargetDirections {
        final Direction primary;
        final Direction secondary;

        TargetDirections(@Nonnull Direction primary, Direction secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        TargetDirections(@Nonnull Direction primary) {
            this(primary, null);
        }

        BlockPos offset(BlockPos initial) {
            return secondary == null ? initial.offset(primary) : initial.offset(primary).offset(secondary);
        }

        static TargetDirections readNBT(CompoundNBT tag) {
            if (!tag.contains("targetDir1")) return null;
            return new TargetDirections(
                    DirectionUtil.VALUES[tag.getInt("targetDir1")],
                    tag.contains("targetDir2") ? DirectionUtil.VALUES[tag.getInt("targetDir2")] : null
            );
        }

        void writeNBT(CompoundNBT tag) {
            tag.putInt("targetDir1", primary.getIndex());
            if (secondary != null) tag.putInt("targetDir2", secondary.getIndex());
        }
    }
}
