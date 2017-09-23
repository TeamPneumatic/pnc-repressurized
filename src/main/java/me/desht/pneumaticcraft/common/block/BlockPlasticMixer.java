package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityPlasticMixer;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockPlasticMixer extends BlockPneumaticCraftModeled {

    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(
            BBConstants.PLASTIC_MIXER_MIN_POS, 0F, BBConstants.PLASTIC_MIXER_MIN_POS,
            BBConstants.PLASTIC_MIXER_MAX_POS, 1, BBConstants.PLASTIC_MIXER_MAX_POS
    );
    private static final AxisAlignedBB COLLISION_BOUNDS = new AxisAlignedBB(
            BBConstants.PLASTIC_MIXER_MIN_POS, 0F, BBConstants.PLASTIC_MIXER_MIN_POS,
            BBConstants.PLASTIC_MIXER_MAX_POS, 1, BBConstants.PLASTIC_MIXER_MAX_POS
    );

    BlockPlasticMixer() {
        super(Material.IRON, "plastic_mixer");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPlasticMixer.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PLASTIC_MIXER;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return COLLISION_BOUNDS;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOCK_BOUNDS;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }
}
