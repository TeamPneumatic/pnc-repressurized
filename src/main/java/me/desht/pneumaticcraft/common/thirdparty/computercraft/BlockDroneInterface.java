package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftModeled;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Collections;
import java.util.Set;

public class BlockDroneInterface extends BlockPneumaticCraftModeled {
    private static final PropertyBool CONNECTED = PropertyBool.create("connected");

    protected BlockDroneInterface() {
        super(Material.IRON, "drone_interface");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONNECTED);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        return te instanceof TileEntityDroneInterface ?
                state.withProperty(CONNECTED, ((TileEntityDroneInterface) te).isDroneConnected()) :
                state;
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDroneInterface.class;
    }

    @Override
    public Set<Item> getApplicableUpgrades() {
        return Collections.emptySet();
    }
}
