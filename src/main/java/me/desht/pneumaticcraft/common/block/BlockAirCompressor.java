package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCompressor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;

public class BlockAirCompressor extends BlockPneumaticCraft {

    public static final BooleanProperty ON = BooleanProperty.create("on");

    public BlockAirCompressor() {
        super("air_compressor");
    }

    BlockAirCompressor(String name) { super(name); }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ON);
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityAirCompressor.class;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }
}
