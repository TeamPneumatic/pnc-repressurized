package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermalCompressor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockThermalCompressor extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB BLOCK_BOUNDS = new AxisAlignedBB(2 / 16F, 0, 2 / 16F, 14 / 16F, 15 / 16F, 14 / 16F);

    BlockThermalCompressor() {
        super(Material.IRON, "thermal_compressor");
    }

    @Override
    public GuiHandler.EnumGuiId getGuiID() {
        return GuiHandler.EnumGuiId.THERMAL_COMPRESSOR;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityThermalCompressor.class;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BLOCK_BOUNDS;
    }
}
