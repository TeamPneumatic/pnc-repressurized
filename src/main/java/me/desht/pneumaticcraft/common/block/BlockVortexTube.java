package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityVortexTube;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.obj.OBJModel.OBJProperty;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collections;

public class BlockVortexTube extends BlockPneumaticCraftModeled {

    private static final OBJState objState = new OBJState(Collections.singletonList("Pipe"), false);

    BlockVortexTube() {
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
                new IProperty[]{ROTATION, BlockPressureTube.DOWN, BlockPressureTube.UP, BlockPressureTube.NORTH, BlockPressureTube.SOUTH, BlockPressureTube.WEST, BlockPressureTube.EAST},
                new IUnlistedProperty[]{OBJProperty.INSTANCE});
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityVortexTube tube = (TileEntityVortexTube) PneumaticCraftUtils.getTileEntitySafely(worldIn, pos); //worldIn.getTileEntity(pos);
        for (int i = 0; i < 6; i++) {
            state = state.withProperty(BlockPressureTube.CONNECTION_PROPERTIES[i], tube.sidesConnected[i]);
        }

        return state;
    }

    /**
     * FIXME doesn't work to remove the pipe obj group, as the model is part of a MultiModel which doesn't implement ISmartModel, so getExtendedState isn't called.
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extended = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        extended = extended.withProperty(OBJProperty.INSTANCE, objState);
        return extended;
    }
}
