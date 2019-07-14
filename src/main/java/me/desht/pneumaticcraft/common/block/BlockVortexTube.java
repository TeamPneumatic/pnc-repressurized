package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockVortexTube extends BlockPneumaticCraftModeled {

    public BlockVortexTube() {
        super(Material.IRON, "vortex_tube");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityVortexTube.class;
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
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[]{ROTATION, BlockPneumaticCraft.DOWN, BlockPneumaticCraft.UP, BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST},
                new IUnlistedProperty[]{OBJProperty.INSTANCE});
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityVortexTube tube = (TileEntityVortexTube) PneumaticCraftUtils.getTileEntitySafely(worldIn, pos); //worldIn.getTileEntity(pos);
        for (int i = 0; i < 6; i++) {
            state = state.withProperty(BlockPneumaticCraft.CONNECTION_PROPERTIES[i], tube.sidesConnected[i]);
        }

        return state;
    }
}
