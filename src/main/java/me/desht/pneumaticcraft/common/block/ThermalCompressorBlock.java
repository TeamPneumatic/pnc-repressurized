package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.common.block.entity.compressor.ThermalCompressorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ThermalCompressorBlock extends AbstractPneumaticCraftBlock
        implements ColorHandlers.IHeatTintable, PneumaticCraftEntityBlock
{
    private static final VoxelShape BOUNDS = Stream.of(
            Block.box(3, 3, 0, 13, 13, 1),
            Block.box(0, 15, 0, 16, 16, 1),
            Block.box(15, 1, 0, 16, 15, 1),
            Block.box(0, 1, 0, 1, 15, 1),
            Block.box(0, 0, 0, 16, 1, 1),
            Block.box(15, 0, 1, 16, 1, 15),
            Block.box(0, 0, 15, 16, 1, 16),
            Block.box(15, 1, 15, 16, 15, 16),
            Block.box(0, 15, 15, 16, 16, 16),
            Block.box(0, 1, 15, 1, 15, 16),
            Block.box(15, 15, 1, 16, 16, 15),
            Block.box(0, 0, 1, 1, 1, 15),
            Block.box(0, 15, 1, 1, 16, 15),
            Block.box(4, 15, 4, 12, 16, 12),
            Block.box(1, 1, 1, 15, 15, 15),
            Block.box(3, 3, 15, 13, 13, 16),
            Block.box(15, 3, 3, 16, 13, 13),
            Block.box(0, 3, 3, 1, 13, 13),
            Block.box(4, 0, 4, 12, 1, 12)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public ThermalCompressorBlock() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected boolean isWaterloggable() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return BOUNDS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ThermalCompressorBlockEntity(pPos, pState);
    }
}
