package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOmnidirectionalHopper extends BlockPneumaticCraftModeled {

    private static final PropertyEnum<EnumFacing> INPUT = PropertyEnum.create("input", EnumFacing.class);

    BlockOmnidirectionalHopper(String registryName) {
        super(Material.IRON, registryName);
    }

    BlockOmnidirectionalHopper() {
        super(Material.IRON, "omnidirectional_hopper");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityOmnidirectionalHopper.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.OMNIDIRECTIONAL_HOPPER;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION, INPUT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(INPUT, EnumFacing.VALUES[meta / 6 % 6]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;//super.getMetaFromState(state) + state.getValue(OUTPUT).ordinal() * 6;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityOmnidirectionalHopper te = (TileEntityOmnidirectionalHopper) PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        return state.withProperty(INPUT, te.getInputDirection()).withProperty(ROTATION, te.getRotation());
    }

    @Override
    protected EnumFacing getRotation(IBlockAccess world, BlockPos pos) {
        TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) world.getTileEntity(pos);
        return hopper.getRotation();
    }

    @Override
    protected void setRotation(World world, BlockPos pos, EnumFacing rotation) {
        TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) world.getTileEntity(pos);
        hopper.setRotation(rotation);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase par5EntityLiving, ItemStack par6ItemStack) {
        super.onBlockPlacedBy(world, pos, state, par5EntityLiving, par6ItemStack);

        TileEntityOmnidirectionalHopper hopper = (TileEntityOmnidirectionalHopper) world.getTileEntity(pos);
        hopper.setRotation(PneumaticCraftUtils.getDirectionFacing(par5EntityLiving, true));
        hopper.setInputDirection(hopper.getRotation().getOpposite());
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
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOmnidirectionalHopper) {
            TileEntityOmnidirectionalHopper teOh = (TileEntityOmnidirectionalHopper) te;
            if (player != null && player.isSneaking()) {
                EnumFacing rotation = getRotation(world, pos);
                rotation = EnumFacing.getFront(rotation.ordinal() + 1);
                if (rotation == teOh.getInputDirection()) rotation = EnumFacing.getFront(rotation.ordinal() + 1);
                setRotation(world, pos, rotation);
            } else {
                EnumFacing rotation = teOh.getInputDirection();
                rotation = EnumFacing.getFront(rotation.ordinal() + 1);
                if (rotation == getRotation(world, pos)) rotation = EnumFacing.getFront(rotation.ordinal() + 1);
                teOh.setInputDirection(rotation);
            }
            return true;
        }
        return false;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d origin, Vec3d direction) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityOmnidirectionalHopper) {
            EnumFacing o = ((TileEntityOmnidirectionalHopper) te).getInputDirection();
            boolean isColliding = false;
            setBlockBounds(new AxisAlignedBB(o.getFrontOffsetX() == 1 ? 10 / 16F : 0, o.getFrontOffsetY() == 1 ? 10 / 16F : 0, o.getFrontOffsetZ() == 1 ? 10 / 16F : 0, o.getFrontOffsetX() == -1 ? 6 / 16F : 1, o.getFrontOffsetY() == -1 ? 6 / 16F : 1, o.getFrontOffsetZ() == -1 ? 6 / 16F : 1));
            if (super.collisionRayTrace(blockState, world, pos, origin, direction) != null) isColliding = true;
            setBlockBounds(new AxisAlignedBB(4 / 16F, 4 / 16F, 4 / 16F, 12 / 16F, 12 / 16F, 12 / 16F));
            if (super.collisionRayTrace(blockState, world, pos, origin, direction) != null) isColliding = true;
            setBlockBounds(FULL_BLOCK_AABB);
            return isColliding ? super.collisionRayTrace(blockState, world, pos, origin, direction) : null;
        }
        return null;
    }
}
