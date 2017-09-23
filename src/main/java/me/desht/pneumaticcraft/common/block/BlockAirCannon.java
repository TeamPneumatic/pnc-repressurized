package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.lib.BBConstants;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockAirCannon extends BlockPneumaticCraftModeled {
    private static final AxisAlignedBB bounds = new AxisAlignedBB(
            BBConstants.AIR_CANNON_MIN_POS_SIDE, 0F, BBConstants.AIR_CANNON_MIN_POS_SIDE,
            BBConstants.AIR_CANNON_MAX_POS_SIDE, BBConstants.AIR_CANNON_MAX_POS_TOP, BBConstants.AIR_CANNON_MAX_POS_SIDE
    );

    BlockAirCannon() {
        super(Material.IRON, "air_cannon");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return bounds;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.AIR_CANNON;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAirCannon.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
