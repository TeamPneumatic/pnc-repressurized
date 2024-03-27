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

package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.network.LazySynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BlockEntityConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public abstract class AbstractAssemblyRobotBlockEntity extends AbstractTickingBlockEntity implements IAssemblyMachine, IResettable {
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

    AbstractAssemblyRobotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

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
            nonNullLevel().getBlockEntity(controllerPos, ModBlockEntityTypes.ASSEMBLY_CONTROLLER.get())
                    .ifPresent(AssemblyControllerBlockEntity::invalidateAssemblySystem);
        }
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        System.arraycopy(angles, 0, oldAngles, 0, angles.length);

        // move the arms and claw toward their destination
        for (int i = 0; i < angles.length; i++) {
            if (angles[i] > targetAngles[i]) {
                angles[i] = Math.max(angles[i] - BlockEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
            } else if (angles[i] < targetAngles[i]) {
                angles[i] = Math.min(angles[i] + BlockEntityConstants.ASSEMBLY_IO_UNIT_ARM_SPEED * (slowMode ? 0.1F : 1) * speed, targetAngles[i]);
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

    BlockEntity getTileEntityForCurrentDirection() {
        if (targetDirection == null) {
            return null;
        } else if (targetDirection.secondary == null) {
            return getCachedNeighbor(targetDirection.primary);
        } else {
            return nonNullLevel().getBlockEntity(targetDirection.offset(getPosition()));
        }
    }

    boolean isDoneMoving() {
        for (int i = 0; i < angles.length; i++) {
            if (!PneumaticCraftUtils.epsilonEquals(angles[i], targetAngles[i])) return false;
        }
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (int i = 0; i < 5; i++) {
            angles[i] = tag.getFloat("angle" + i);
            targetAngles[i] = tag.getFloat("targetAngle" + i);
        }
        slowMode = tag.getBoolean("slowMode");
        speed = tag.getFloat("speed");
        targetDirection = TargetDirections.readNBT(tag);

    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        for (int i = 0; i < 5; i++) {
            tag.putFloat("angle" + i, angles[i]);
            tag.putFloat("targetAngle" + i, targetAngles[i]);
        }
        tag.putBoolean("slowMode", slowMode);
        tag.putFloat("speed", speed);
        if (targetDirection != null) targetDirection.writeNBT(tag);
    }

    protected abstract boolean canMoveToDiagonalNeighbours();

    TargetDirections getPlatformDirection() {
        Level level = nonNullLevel();
        for (Direction dir : DirectionUtil.HORIZONTALS) {
            if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof AssemblyPlatformBlockEntity)
                return new TargetDirections(dir);
        }
        if (canMoveToDiagonalNeighbours()) {
            for (Direction secDir : new Direction[]{Direction.WEST, Direction.EAST}) {
                for (Direction primDir : new Direction[]{Direction.NORTH, Direction.SOUTH}) {
                    if (level.getBlockEntity(getBlockPos().relative(primDir).relative(secDir)) instanceof AssemblyPlatformBlockEntity) {
                        return new TargetDirections(primDir, secDir);
                    }
                }
            }
        }
        return null;
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
     * A compound direction relative to the block entity in question. First direction is always non-null, second
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
            return secondary == null ? initial.relative(primary) : initial.relative(primary).relative(secondary);
        }

        static TargetDirections readNBT(CompoundTag tag) {
            if (!tag.contains("targetDir1")) return null;
            return new TargetDirections(
                    Direction.from3DDataValue(tag.getInt("targetDir1")),
                    tag.contains("targetDir2") ? Direction.from3DDataValue(tag.getInt("targetDir2")) : null
            );
        }

        void writeNBT(CompoundTag tag) {
            tag.putInt("targetDir1", primary.get3DDataValue());
            if (secondary != null) tag.putInt("targetDir2", secondary.get3DDataValue());
        }
    }
}
