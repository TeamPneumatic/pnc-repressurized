package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftModeled;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collections;
import java.util.Set;

public class BlockDroneInterface extends BlockPneumaticCraftModeled {

    protected BlockDroneInterface(Material material) {
        super(material, "drone_interface");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDroneInterface.class;
    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return side == EnumFacing.DOWN;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return Collections.EMPTY_SET;
    }
}
