package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.lib.BBConstants;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockHeatSink extends BlockPneumaticCraftModeled {

    BlockHeatSink() {
        super(Material.IRON, "heat_sink");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityHeatSink.class;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (!source.getBlockState(pos).getPropertyKeys().contains(ROTATION)) {
            // getBoundingBox() can be called during placement (from World#mayPlace), before the
            // block is actually placed; handle this, or we'll crash with an IllegalArgumentException
            return FULL_BLOCK_AABB;
        }

        EnumFacing dir = getRotation(source, pos);
        return new AxisAlignedBB(
                dir.getFrontOffsetX() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getFrontOffsetY() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getFrontOffsetZ() <= 0 ? 0 : 1F - BBConstants.HEAT_SINK_THICKNESS,
                dir.getFrontOffsetX() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS,
                dir.getFrontOffsetY() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS,
                dir.getFrontOffsetZ() >= 0 ? 1 : BBConstants.HEAT_SINK_THICKNESS
        );
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(ROTATION, facing.getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
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
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        TileEntityHeatSink heatSink = (TileEntityHeatSink) world.getTileEntity(pos);
        if (heatSink != null && heatSink.getHeatExchangerLogic(null).getTemperature() > 323) {
            entity.setFire(3);
        }
    }
}
