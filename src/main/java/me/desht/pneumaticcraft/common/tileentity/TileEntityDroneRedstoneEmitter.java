package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.common.core.ModTileEntities;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class TileEntityDroneRedstoneEmitter extends TileEntity implements ITickableTileEntity {
    public TileEntityDroneRedstoneEmitter() {
        super(ModTileEntities.DRONE_REDSTONE_EMITTER.get());
    }

    @Override
    public void tick() {
        BlockState state = getLevel().getBlockState(getBlockPos());
        for (Direction facing : DirectionUtil.VALUES) {
            if (state.getSignal(getLevel(), getBlockPos(),  facing) > 0) {
                return;
            }
        }
        getLevel().removeBlock(getBlockPos(), false);
    }
}
