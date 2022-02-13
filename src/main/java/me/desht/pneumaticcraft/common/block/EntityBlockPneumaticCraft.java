package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.block.entity.AbstractTickingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface EntityBlockPneumaticCraft extends EntityBlock {
    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof AbstractTickingBlockEntity tickable) {
                tickable.tickCommonPre();
                if (level1.isClientSide()) {
                    tickable.tickClient();
                } else {
                    tickable.tickServer();
                }
                tickable.tickCommonPost();
            }
        };
    }
}
