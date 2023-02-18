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

package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.PneumaticDoorBaseBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.PneumaticDoorBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PneumaticDoorBlock extends AbstractPneumaticCraftBlock implements PneumaticCraftEntityBlock {
    public static final BooleanProperty TOP_DOOR = BooleanProperty.create("top_door");

    public PneumaticDoorBlock() {
        super(ModBlocks.defaultProps());

        registerDefaultState(defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, false)
                .setValue(TOP_DOOR, false));
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP_DOOR, DoorBlock.OPEN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public static boolean isTopDoor(BlockState state) {
        return state.getBlock() == ModBlocks.PNEUMATIC_DOOR.get() && state.getValue(TOP_DOOR);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return calculateVoxelShape(state, world, pos, 13);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return calculateVoxelShape(state, world, pos, 15);
    }

    private VoxelShape calculateVoxelShape(BlockState state, BlockGetter world, BlockPos pos, int thickness) {
        float xMin = 0.001f;
        float zMin = 0.001f;
        float xMax = 0.999f;
        float zMax = 0.999f;
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof PneumaticDoorBlockEntity door) {
            Direction rotation = getRotation(state);
            float t = thickness / 16F;
            float rads = (float) Math.toRadians(door.rotationAngle);
            float cosinus = t - Mth.sin(rads) * t;
            float sinus = t - Mth.cos(rads) * t;
            if (door.rightGoing) {
                switch (rotation) {
                    case NORTH -> {
                        zMin = cosinus;
                        xMax = 1 - sinus;
                    }
                    case WEST -> {
                        xMin = cosinus;
                        zMin = sinus;
                    }
                    case SOUTH -> {
                        zMax = 1 - cosinus;
                        xMin = sinus;
                    }
                    case EAST -> {
                        xMax = 1 - cosinus;
                        zMax = 1 - sinus;
                    }
                }
            } else {
                switch (rotation) {
                    case NORTH -> {
                        zMin = cosinus;
                        xMin = sinus;
                    }
                    case WEST -> {
                        xMin = 0.001f + cosinus;
                        zMax = 0.999f - sinus;
                    }
                    case SOUTH -> {
                        zMax = 1 - cosinus;
                        xMax = 1 - sinus;
                    }
                    case EAST -> {
                        xMax = 1 - cosinus;
                        zMin = sinus;
                    }
                }
            }
        }
        boolean topDoor = state.getValue(TOP_DOOR);
        return Shapes.create(new AABB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(TOP_DOOR)) {
            return world.getBlockState(pos.below()).getBlock() == this;
        } else {
            return world.isEmptyBlock(pos.above());// && Block.hasEnoughSolidSide(world, pos.down(), Direction.UP);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.setPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        world.setBlock(pos.relative(Direction.UP), world.getBlockState(pos).setValue(TOP_DOOR, true), Block.UPDATE_ALL);
        world.getBlockEntity(pos, ModBlockEntities.PNEUMATIC_DOOR.get()).ifPresent(teDoor -> {
            BlockPos top = pos.above();
            if (world.getBlockState(top.relative(getRotation(state).getCounterClockWise())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = true;
            } else if (world.getBlockState(top.relative(getRotation(state).getClockWise())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = false;
            }
            BlockEntity topHalf = world.getBlockEntity(top);
            if (topHalf instanceof PneumaticDoorBlockEntity door) {
                door.rightGoing = teDoor.rightGoing;
                door.color = teDoor.color;
            }
        });
    }

    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        BlockPos posDown = pos.below();
        BlockPos posUp = pos.above();
        if (isTopDoor(state) && worldIn.getBlockState(posDown).getBlock() == this) {
            worldIn.removeBlock(posDown, false);
        } else if (!isTopDoor(state) && worldIn.getBlockState(posUp).getBlock() == this) {
            worldIn.removeBlock(posUp, false);
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    public boolean onWrenched(Level world, Player player, BlockPos pos, Direction face, InteractionHand hand) {
        BlockState state = world.getBlockState(pos);
        if (isTopDoor(state)) {
            return onWrenched(world, player, pos.relative(Direction.DOWN), face, hand);
        }
        if (player != null && player.isShiftKeyDown()) {
            if (!player.isCreative()) {
                BlockEntity te = world.getBlockEntity(pos);
                Block.dropResources(world.getBlockState(pos), world, pos, te);
                removeBlockSneakWrenched(world, pos);
                removeBlockSneakWrenched(world, pos.above());
            }
        } else {
            super.onWrenched(world, player, pos, face, hand);
            BlockState newState = world.getBlockState(pos);
            world.setBlock(pos.relative(Direction.UP), newState.setValue(TOP_DOOR, true), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        PneumaticDoorBaseBlockEntity doorBase = getDoorBase(world, pos);
        if (!world.isClientSide) {
            DyeColor dyeColor =  DyeColor.getColor(player.getItemInHand(hand));
            if (dyeColor != null) {
                BlockEntity te = world.getBlockEntity(isTopDoor(state) ? pos.below() : pos);
                if (te instanceof PneumaticDoorBlockEntity teDoor) {
                    if (teDoor.setColor(dyeColor) && ConfigHelper.common().general.useUpDyesWhenColoring.get()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                }
            } else if (doorBase != null && doorBase.getRedstoneController().getCurrentMode() == PneumaticDoorBaseBlockEntity.RS_MODE_WOODEN_DOOR
                    && doorBase.getPressure() >= doorBase.getMinWorkingPressure() && hand == InteractionHand.MAIN_HAND) {
                doorBase.setOpening(!doorBase.isOpening());
                doorBase.setNeighborOpening(doorBase.isOpening());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.getBestNeighborSignal(pos) > 0;
        if (!powered) {
            powered = world.getBestNeighborSignal(pos.relative(isTopDoor(state) ? Direction.DOWN : Direction.UP)) > 0;
        }
        PneumaticDoorBaseBlockEntity doorBase = getDoorBase(world, pos);
        if (!world.isClientSide && doorBase != null && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if (powered != doorBase.wasPowered) {
                doorBase.wasPowered = powered;
                doorBase.setOpening(powered);
                doorBase.setNeighborOpening(doorBase.isOpening());
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return state.getValue(DoorBlock.OPEN);
    }

    private PneumaticDoorBaseBlockEntity getDoorBase(BlockGetter world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != this) return null;
        if (!isTopDoor(world.getBlockState(pos))) {
            return getDoorBase(world, pos.relative(Direction.UP));
        } else {
            Direction dir = getRotation(world, pos);
            if (dir.getAxis() == Direction.Axis.Y) {
                // should never happen, but see https://github.com/TeamPneumatic/pnc-repressurized/issues/284
                return null;
            }
            BlockEntity te1 = world.getBlockEntity(pos.relative(dir.getClockWise()));
            if (te1 instanceof PneumaticDoorBaseBlockEntity doorBase && doorBase.getRotation() == dir.getCounterClockWise()) {
                return doorBase;
            }
            BlockEntity te2 = world.getBlockEntity(pos.relative(dir.getCounterClockWise()));
            if (te2 instanceof PneumaticDoorBaseBlockEntity doorBase && doorBase.getRotation() == dir.getClockWise()) {
                return doorBase;
            }
            return null;
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PneumaticDoorBlockEntity(pPos, pState);
    }

    public static class ItemBlockPneumaticDoor extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockPneumaticDoor(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            if (tintIndex == 0) {
                CompoundTag tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
                if (tag != null && tag.contains("color", Tag.TAG_INT)) {
                    int color = tag.getInt("color");
                    return PneumaticCraftUtils.getDyeColorAsRGB(DyeColor.byId(color));
                }
            }
            return 0xFFFFFFFF;
        }
    }
}
