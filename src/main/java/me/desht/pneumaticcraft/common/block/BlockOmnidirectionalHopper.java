package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BlockOmnidirectionalHopper extends BlockPneumaticCraft {

    VoxelShape INPUT_SHAPE = Block.makeCuboidShape(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    VoxelShape BOWL_SHAPE = Block.makeCuboidShape(2.0D, 11.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    VoxelShape INPUT_CUTOUT = VoxelShapes.combineAndSimplify(INPUT_SHAPE, BOWL_SHAPE, IBooleanFunction.ONLY_FIRST);
    VoxelShape MIDDLE_SHAPE = Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);

    // superclass ROTATION property is used for the output direction

    public static final EnumProperty<Direction> INPUT = EnumProperty.create("input", Direction.class);

    BlockOmnidirectionalHopper(String registryName) {
        super(Material.IRON, registryName);
    }

    public BlockOmnidirectionalHopper() {
        super(Material.IRON, "omnidirectional_hopper");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityOmnidirectionalHopper.class;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(INPUT);
    }

    @Override
    public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
        // todo 1.14 no placer entity available - intended or bug?
        return state.with(ROTATION, facing.getOpposite()).with(INPUT, facing);
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    public Direction getInputDirection(World world, BlockPos pos) {
        return world.getBlockState(pos).get(BlockOmnidirectionalHopper.INPUT);
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
        BlockState state = world.getBlockState(pos);
        if (player != null && player.isSneaking()) {
            Direction outputDir = state.get(ROTATION);
            outputDir = Direction.byIndex(outputDir.ordinal() + 1);
            if (outputDir == getInputDirection(world, pos)) outputDir = Direction.byIndex(outputDir.ordinal() + 1);
            world.setBlockState(pos, state.with(ROTATION, outputDir));
        } else {
            Direction inputDir = state.get(INPUT);
            inputDir = Direction.byIndex(inputDir.ordinal() + 1);
            if (inputDir == getRotation(world, pos)) inputDir = Direction.byIndex(inputDir.ordinal() + 1);
            world.setBlockState(pos, state.with(INPUT, inputDir));
        }
        return true;
    }

    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOmnidirectionalHopper) {
            Direction o = ((TileEntityOmnidirectionalHopper) te).getInputDirection();
            boolean isColliding = false;
            setBlockBounds(new AxisAlignedBB(o.getXOffset() == 1 ? 10 / 16F : 0, o.getYOffset() == 1 ? 10 / 16F : 0, o.getZOffset() == 1 ? 10 / 16F : 0, o.getXOffset() == -1 ? 6 / 16F : 1, o.getYOffset() == -1 ? 6 / 16F : 1, o.getZOffset() == -1 ? 6 / 16F : 1));
            if (super.collisionRayTrace(blockState, world, pos, origin, direction) != null) isColliding = true;
            setBlockBounds(new AxisAlignedBB(4 / 16F, 4 / 16F, 4 / 16F, 12 / 16F, 12 / 16F, 12 / 16F));
            if (super.collisionRayTrace(blockState, world, pos, origin, direction) != null) isColliding = true;
            setBlockBounds(FULL_BLOCK_AABB);
            return isColliding ? super.collisionRayTrace(blockState, world, pos, origin, direction) : null;
        }
        return null;
    }
}
