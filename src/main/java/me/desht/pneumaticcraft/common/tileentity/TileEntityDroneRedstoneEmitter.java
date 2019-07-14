package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;

public class TileEntityDroneRedstoneEmitter extends TileEntity implements ITickable {
    @Override
    public void tick() {
        BlockState state = getWorld().getBlockState(getPos());
        for (Direction facing : Direction.VALUES) {
            if (state.getWeakPower(getWorld(), getPos(),  facing) > 0) {
                return;
            }
        }
        getWorld().setBlockToAir(getPos());
    }
}
