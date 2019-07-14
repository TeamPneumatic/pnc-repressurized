package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockOmnidirectionalHopper extends BlockPneumaticCraft {

    private static final EnumProperty<Direction> INPUT = EnumProperty.create("input", Direction.class);

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
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityOmnidirectionalHopper te = (TileEntityOmnidirectionalHopper) PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        return state.withProperty(INPUT, te.getInputDirection()).withProperty(ROTATION, te.getRotation());
    }

    @Override
    protected Direction getRotation(IBlockReader world, BlockPos pos) {
        TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) world.getTileEntity(pos);
        return hopper.getRotation();
    }

    @Override
    protected void setRotation(World world, BlockPos pos, Direction rotation) {
        TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) world.getTileEntity(pos);
        hopper.setRotation(rotation);
    }

//    @Override
//    public BlockState getStateForPlacement(BlockState state, Direction facing, BlockState state2, IWorld world, BlockPos pos1, BlockPos pos2, Hand hand) {
//        return this.getDefaultState()
//                .with(ROTATION, facing.getOpposite())
//                .with(INPUT, PneumaticCraftUtils.getDirectionFacing(placer, true).getOpposite());
//    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOmnidirectionalHopper) {
            TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) te;
            hopper.setInputDirection(state.get(INPUT));
            hopper.setRotation(state.get(ROTATION));
        }
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return true;
    }

    @Override
    public boolean onWrenched(World world, PlayerEntity player, BlockPos pos, Direction face, Hand hand) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOmnidirectionalHopper) {
            TileEntityOmnidirectionalHopper teOh = (TileEntityOmnidirectionalHopper) te;
            if (player != null && player.isSneaking()) {
                Direction rotation = getRotation(world, pos);
                rotation = Direction.byIndex(rotation.ordinal() + 1);
                if (rotation == teOh.getInputDirection()) rotation = Direction.byIndex(rotation.ordinal() + 1);
                setRotation(world, pos, rotation);
            } else {
                Direction rotation = teOh.getInputDirection();
                rotation = Direction.byIndex(rotation.ordinal() + 1);
                if (rotation == getRotation(world, pos)) rotation = Direction.byIndex(rotation.ordinal() + 1);
                teOh.setInputDirection(rotation);
            }
            return true;
        }
        return false;
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
