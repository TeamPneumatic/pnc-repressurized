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

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.utility.PneumaticDoorBaseBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.utility.PneumaticDoorBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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

import java.util.List;

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
        world.getBlockEntity(pos, ModBlockEntityTypes.PNEUMATIC_DOOR.get()).ifPresent(teDoor -> {
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
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        BlockPos posDown = pos.below();
        BlockPos posUp = pos.above();
        if (isTopDoor(state) && worldIn.getBlockState(posDown).getBlock() == this) {
            worldIn.removeBlock(posDown, false);
        } else if (!isTopDoor(state) && worldIn.getBlockState(posUp).getBlock() == this) {
            worldIn.removeBlock(posUp, false);
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult brtr) {
        DyeColor dyeColor =  DyeColor.getColor(player.getItemInHand(hand));
        if (dyeColor != null) {
            if (!world.isClientSide) {
                BlockEntity te = world.getBlockEntity(isTopDoor(state) ? pos.below() : pos);
                if (te instanceof PneumaticDoorBlockEntity teDoor) {
                    if (teDoor.setColor(dyeColor) && ConfigHelper.common().general.useUpDyesWhenColoring.get()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult brtr) {
        PneumaticDoorBaseBlockEntity doorBase = getDoorBase(world, pos);
        if (!world.isClientSide) {
            if (doorBase != null
                    && doorBase.getRedstoneController().getCurrentMode() == PneumaticDoorBaseBlockEntity.RS_MODE_WOODEN_DOOR
                    && doorBase.getPressure() >= doorBase.getMinWorkingPressure()) {
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
    public boolean isPathfindable(BlockState state, PathComputationType type) {
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

    @Override
    public void addSerializableComponents(List<DataComponentType<?>> list) {
        super.addSerializableComponents(list);
        list.add(ModDataComponents.DOOR_COLOR.get());
    }

    public static class ItemBlockPneumaticDoor extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockPneumaticDoor(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            if (tintIndex == 0) {
                return stack.getOrDefault(ModDataComponents.DOOR_COLOR, DyeColor.WHITE).getTextureDiffuseColor();
            }
            return 0xFFFFFFFF;
        }
    }
}
