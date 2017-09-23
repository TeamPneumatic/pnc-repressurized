package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAssemblyIOUnit extends BlockPneumaticCraftModeled {
    private static final PropertyBool IMPORT = PropertyBool.create("import");

    private static final AxisAlignedBB BASE_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_ROBOT_MIN_POS, 0F, BBConstants.ASSEMBLY_ROBOT_MIN_POS,
            BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS);
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS,
            BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS);

    BlockAssemblyIOUnit() {
        super(Material.IRON, "assembly_io_unit");
        setBlockBounds(BASE_BOUNDS);
    }

    @Override
    public boolean rotateBlock(World world, EntityPlayer player, BlockPos pos, EnumFacing side) {
        if (player.isSneaking()) {
            return super.rotateBlock(world, player, pos, side);
        } else {
            return ((TileEntityAssemblyIOUnit) world.getTileEntity(pos)).switchMode();
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IMPORT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;  // import/export value is stored in the TE
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityAssemblyIOUnit) {
            boolean importing = ((TileEntityAssemblyIOUnit) te).isImportUnit();
            return state.withProperty(IMPORT, importing);
        }
        return state;
    }

    //    @Override
//    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, BlockPos pos) {
//        setBlockBounds(BBConstants.ASSEMBLY_ROBOT_MIN_POS, 0F, BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS);
//    }
//
//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBounds(BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MIN_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS, BBConstants.ASSEMBLY_ROBOT_MAX_POS_TOP, BBConstants.ASSEMBLY_ROBOT_MAX_POS);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyIOUnit.class;
    }

//    @Override
//    //Overriden here because the IOUnit isn't implementing IInventory (intentionally).
//    protected void dropInventory(World world, BlockPos pos) {
//        TileEntity tileEntity = world.getTileEntity(pos);
//
//        if (!(tileEntity instanceof TileEntityAssemblyIOUnit)) return;
//
//        TileEntityAssemblyIOUnit ioUnit = (TileEntityAssemblyIOUnit) tileEntity;
//        Random rand = new Random();
//
//        ItemStack itemStack = ioUnit.getPrimaryInventory().getStackInSlot(0);
//
//        if (itemStack.getCount() > 0) {
//            float dX = rand.nextFloat() * 0.8F + 0.1F;
//            float dY = rand.nextFloat() * 0.8F + 0.1F;
//            float dZ = rand.nextFloat() * 0.8F + 0.1F;
//
//            EntityItem entityItem = new EntityItem(world, pos.getX() + dX, pos.getY() + dY, pos.getZ() + dZ, itemStack.copy());
//
//            float factor = 0.05F;
//            entityItem.motionX = rand.nextGaussian() * factor;
//            entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
//            entityItem.motionZ = rand.nextGaussian() * factor;
//            world.spawnEntity(entityItem);
//            itemStack.setCount(0);
//        }
//    }

}
