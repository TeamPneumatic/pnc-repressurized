package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockAssemblyPlatform extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_PLATFORM_MIN_POS, 0F, BBConstants.ASSEMBLY_PLATFORM_MIN_POS,
            BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS,
            BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS
    );

    BlockAssemblyPlatform() {
        super(Material.IRON, "assembly_platform");
        setBlockBounds(BLOCK_BOUNDS);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

//    @Override
//    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, BlockPos pos) {
//        setBlockBounds(BBConstants.ASSEMBLY_PLATFORM_MIN_POS, 0F, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS);
//    }
//
//    @Override
//    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
//        setBlockBounds(BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MIN_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS, BBConstants.ASSEMBLY_PLATFORM_MAX_POS_TOP, BBConstants.ASSEMBLY_PLATFORM_MAX_POS);
//        super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
//        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
//    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAssemblyPlatform.class;
    }

    //overriden here because the Assembly Platform isn't implementing IInventory (intentionally).
//    @Override
//    protected void dropInventory(World world, BlockPos pos) {
//        // FIXME use IItemHandler
//        TileEntity tileEntity = world.getTileEntity(pos);
//        if (!(tileEntity instanceof TileEntityAssemblyPlatform)) return;
//        TileEntityAssemblyPlatform inventory = (TileEntityAssemblyPlatform) tileEntity;
//        Random rand = new Random();
//        ItemStack itemStack = inventory.getHeldStack();
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
