package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class BlockPneumaticDoor extends BlockPneumaticCraft {
    public static final BooleanProperty TOP_DOOR = BooleanProperty.create("top_door");

    public BlockPneumaticDoor() {
        super(ModBlocks.defaultProps());

        registerDefaultState(getStateDefinition().any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(DoorBlock.OPEN, false)
                .setValue(TOP_DOOR, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP_DOOR, DoorBlock.OPEN);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public static boolean isTopDoor(BlockState state) {
        return state.getBlock() == ModBlocks.PNEUMATIC_DOOR.get() && state.getValue(TOP_DOOR);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return calculateVoxelShape(state, world, pos, 13);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return calculateVoxelShape(state, world, pos, 15);
    }

    private VoxelShape calculateVoxelShape(BlockState state, IBlockReader world, BlockPos pos, int thickness) {
        float xMin = 0.001f;
        float zMin = 0.001f;
        float xMax = 0.999f;
        float zMax = 0.999f;
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEntityPneumaticDoor) {
            Direction rotation = getRotation(state);
            TileEntityPneumaticDoor door = (TileEntityPneumaticDoor) te;
            float t = thickness / 16F;
            float rads = (float) Math.toRadians(door.rotationAngle);
            float cosinus = t - MathHelper.sin(rads) * t;
            float sinus = t - MathHelper.cos(rads) * t;
            if (door.rightGoing) {
                switch (rotation) {
                    case NORTH: zMin = cosinus; xMax = 1 - sinus; break;
                    case WEST: xMin = cosinus; zMin = sinus; break;
                    case SOUTH: zMax = 1 - cosinus; xMin = sinus; break;
                    case EAST: xMax = 1 - cosinus; zMax = 1 - sinus; break;
                }
            } else {
                switch (rotation) {
                    case NORTH: zMin = cosinus; xMin = sinus; break;
                    case WEST: xMin = 0.001f + cosinus; zMax = 0.999f - sinus; break;
                    case SOUTH: zMax = 1 - cosinus; xMax = 1 - sinus; break;
                    case EAST: xMax = 1 - cosinus; zMin = sinus; break;
                }
            }
        }
        boolean topDoor = state.getValue(TOP_DOOR);
        return VoxelShapes.create(new AxisAlignedBB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoor.class;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        if (state.getValue(TOP_DOOR)) {
            return world.getBlockState(pos.below()).getBlock() == this;
        } else {
            return world.isEmptyBlock(pos.above());// && Block.hasEnoughSolidSide(world, pos.down(), Direction.UP);
        }
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.setPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        world.setBlock(pos.relative(Direction.UP), world.getBlockState(pos).setValue(TOP_DOOR, true), Constants.BlockFlags.DEFAULT);
        PneumaticCraftUtils.getTileEntityAt(world, pos, TileEntityPneumaticDoor.class).ifPresent(teDoor -> {
            BlockPos top = pos.above();
            if (world.getBlockState(top.relative(getRotation(state).getCounterClockWise())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = true;
            } else if (world.getBlockState(top.relative(getRotation(state).getClockWise())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = false;
            }
            TileEntity topHalf = world.getBlockEntity(top);
            if (topHalf instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) topHalf).rightGoing = teDoor.rightGoing;
                ((TileEntityPneumaticDoor) topHalf).color = teDoor.color;
            }
        });
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
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
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
        BlockState state = world.getBlockState(pos);
        if (isTopDoor(state)) {
            return onWrenched(world, player, pos.relative(Direction.DOWN), face, hand);
        }
        if (player != null && player.isShiftKeyDown()) {
            if (!player.isCreative()) {
                TileEntity te = world.getBlockEntity(pos);
                Block.dropResources(world.getBlockState(pos), world, pos, te);
                removeBlockSneakWrenched(world, pos);
                removeBlockSneakWrenched(world, pos.above());
            }
        } else {
            super.onWrenched(world, player, pos, face, hand);
            BlockState newState = world.getBlockState(pos);
            world.setBlock(pos.relative(Direction.UP), newState.setValue(TOP_DOOR, true), Constants.BlockFlags.DEFAULT);
        }
        return true;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isClientSide) {
            DyeColor dyeColor =  DyeColor.getColor(player.getItemInHand(hand));
            if (dyeColor != null) {
                TileEntity te = world.getBlockEntity(isTopDoor(state) ? pos.below() : pos);
                if (te instanceof TileEntityPneumaticDoor) {
                    TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
                    if (teDoor.setColor(dyeColor) && ConfigHelper.common().general.useUpDyesWhenColoring.get()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                }
            } else if (doorBase != null && doorBase.getRedstoneController().getCurrentMode() == TileEntityPneumaticDoorBase.RS_MODE_WOODEN_DOOR
                    && doorBase.getPressure() >= doorBase.getMinWorkingPressure() && hand == Hand.MAIN_HAND) {
                doorBase.setOpening(!doorBase.isOpening());
                doorBase.setNeighborOpening(doorBase.isOpening());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = world.getBestNeighborSignal(pos) > 0;
        if (!powered) {
            powered = world.getBestNeighborSignal(pos.relative(isTopDoor(state) ? Direction.DOWN : Direction.UP)) > 0;
        }
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isClientSide && doorBase != null && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if (powered != doorBase.wasPowered) {
                doorBase.wasPowered = powered;
                doorBase.setOpening(powered);
                doorBase.setNeighborOpening(doorBase.isOpening());
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return state.getValue(DoorBlock.OPEN);
    }

    private TileEntityPneumaticDoorBase getDoorBase(IBlockReader world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != this) return null;
        if (!isTopDoor(world.getBlockState(pos))) {
            return getDoorBase(world, pos.relative(Direction.UP));
        } else {
            Direction dir = getRotation(world, pos);
            if (dir.getAxis() == Direction.Axis.Y) {
                // should never happen, but see https://github.com/TeamPneumatic/pnc-repressurized/issues/284
                return null;
            }
            TileEntity te1 = world.getBlockEntity(pos.relative(dir.getClockWise()));
            if (te1 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) te1;
                if (doorBase.getRotation() == dir.getCounterClockWise()) {
                    return doorBase;
                }
            }
            TileEntity te2 = world.getBlockEntity(pos.relative(dir.getCounterClockWise()));
            if (te2 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) te2;
                if (doorBase.getRotation() == dir.getClockWise()) {
                    return doorBase;
                }
            }
            return null;
        }
    }

    public static class ItemBlockPneumaticDoor extends BlockItem implements ColorHandlers.ITintableItem {
        public ItemBlockPneumaticDoor(Block blockIn) {
            super(blockIn, ModItems.defaultProps());
        }

        @Override
        public int getTintColor(ItemStack stack, int tintIndex) {
            if (tintIndex == 0) {
                CompoundNBT tag = stack.getTagElement(NBTKeys.BLOCK_ENTITY_TAG);
                if (tag != null && tag.contains("color", Constants.NBT.TAG_INT)) {
                    int color = tag.getInt("color");
                    return DyeColor.byId(color).getColorValue();
                }
            }
            return 0xFFFFFFFF;
        }
    }
}
