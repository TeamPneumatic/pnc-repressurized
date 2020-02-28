package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoorBase;
import me.desht.pneumaticcraft.lib.NBTKeys;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
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

        setDefaultState(getStateContainer().getBaseState()
                .with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .with(TOP_DOOR, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(TOP_DOOR);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    public static boolean isTopDoor(BlockState state) {
        return state.getBlock() == ModBlocks.PNEUMATIC_DOOR.get() && state.get(TOP_DOOR);
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
        TileEntity te = world.getTileEntity(pos);
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
        boolean topDoor = state.get(TOP_DOOR);
        return VoxelShapes.create(new AxisAlignedBB(xMin, topDoor ? -1 : 0, zMin, xMax, topDoor ? 1 : 2, zMax));
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDoor.class;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos down = pos.down();
        BlockState belowState = world.getBlockState(down);
        if (state.get(TOP_DOOR)) {
            return belowState.getBlock() == this;
        } else {
            return Block.hasSolidSide(belowState, world, down, Direction.UP);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        world.setBlockState(pos.offset(Direction.UP), world.getBlockState(pos).with(TOP_DOOR, true), Constants.BlockFlags.DEFAULT);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityPneumaticDoor) {
            TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
            BlockPos top = pos.up();
            if (world.getBlockState(top.offset(getRotation(state).rotateYCCW())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = true;
            } else if (world.getBlockState(top.offset(getRotation(state).rotateY())).getBlock() == ModBlocks.PNEUMATIC_DOOR_BASE.get()) {
                teDoor.rightGoing = false;
            }
            TileEntity topHalf = world.getTileEntity(top);
            if (topHalf instanceof TileEntityPneumaticDoor) {
                ((TileEntityPneumaticDoor) topHalf).rightGoing = teDoor.rightGoing;
                ((TileEntityPneumaticDoor) topHalf).color = teDoor.color;
            }
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos posDown = pos.down();
        BlockPos posUp = pos.up();
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
            return onWrenched(world, player, pos.offset(Direction.DOWN), face, hand);
        }
        if (player != null && player.isSneaking()) {
            if (!player.isCreative()) {
                TileEntity te = world.getTileEntity(pos);
                Block.spawnDrops(world.getBlockState(pos), world, pos, te);
                IFluidState ifluidstate = world.getFluidState(pos);
                world.setBlockState(pos, ifluidstate.getBlockState(), Constants.BlockFlags.DEFAULT);
                ifluidstate = world.getFluidState(pos.up());
                world.setBlockState(pos.up(), ifluidstate.getBlockState(), Constants.BlockFlags.DEFAULT);
            }
        } else {
            super.onWrenched(world, player, pos, face, hand);
            BlockState newState = world.getBlockState(pos);
            world.setBlockState(pos.offset(Direction.UP), newState.with(TOP_DOOR, true), Constants.BlockFlags.DEFAULT);
        }
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote) {
            DyeColor dyeColor =  DyeColor.getColor(player.getHeldItem(hand));
            if (dyeColor != null) {
                TileEntity te = world.getTileEntity(isTopDoor(state) ? pos.down() : pos);
                if (te instanceof TileEntityPneumaticDoor) {
                    TileEntityPneumaticDoor teDoor = (TileEntityPneumaticDoor) te;
                    if (teDoor.setColor(dyeColor) && PNCConfig.Common.General.useUpDyesWhenColoring) {
                        player.getHeldItem(hand).shrink(1);
                    }
                }
                return true;
            } else if (doorBase != null && doorBase.redstoneMode == 2
                    && doorBase.getPressure() >= doorBase.getMinWorkingPressure() && hand == Hand.MAIN_HAND) {
                doorBase.setOpening(!doorBase.isOpening());
                doorBase.setNeighborOpening(doorBase.isOpening());
                return true;
            }
        }
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean b) {
        boolean powered = world.getRedstonePowerFromNeighbors(pos) > 0;
        if (!powered) {
            powered = world.getRedstonePowerFromNeighbors(pos.offset(isTopDoor(state) ? Direction.DOWN : Direction.UP)) > 0;
        }
        TileEntityPneumaticDoorBase doorBase = getDoorBase(world, pos);
        if (!world.isRemote && doorBase != null && doorBase.getPressure() >= PneumaticValues.MIN_PRESSURE_PNEUMATIC_DOOR) {
            if (powered != doorBase.wasPowered) {
                doorBase.wasPowered = powered;
                doorBase.setOpening(powered);
            }
        }
    }

    private TileEntityPneumaticDoorBase getDoorBase(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() != this) return null;
        if (!isTopDoor(world.getBlockState(pos))) {
            return getDoorBase(world, pos.offset(Direction.UP));
        } else {
            Direction dir = getRotation(world, pos);
            if (dir.getAxis() == Direction.Axis.Y) {
                // should never happen, but see https://github.com/TeamPneumatic/pnc-repressurized/issues/284
                return null;
            }
            TileEntity te1 = world.getTileEntity(pos.offset(dir.rotateY()));
            if (te1 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) te1;
                if (doorBase.getRotation() == dir.rotateYCCW()) {
                    return doorBase;
                }
            }
            TileEntity te2 = world.getTileEntity(pos.offset(dir.rotateYCCW()));
            if (te2 instanceof TileEntityPneumaticDoorBase) {
                TileEntityPneumaticDoorBase doorBase = (TileEntityPneumaticDoorBase) te2;
                if (doorBase.getRotation() == dir.rotateY()) {
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
                CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
                if (tag != null && tag.contains("color", Constants.NBT.TAG_INT)) {
                    int color = tag.getInt("color");
                    return DyeColor.byId(color).getColorValue();
                }
            }
            return 0xFFFFFFFF;
        }
    }
}
