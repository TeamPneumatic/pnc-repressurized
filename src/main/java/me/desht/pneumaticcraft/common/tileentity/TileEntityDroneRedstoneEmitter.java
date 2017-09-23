package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityDroneRedstoneEmitter extends TileEntity implements ITickable {
    @Override
    public void update() {
        IBlockState state = getWorld().getBlockState(getPos());
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (state.getWeakPower(getWorld(), getPos(),  facing) > 0) {
                return;
            }
        }
        getWorld().setBlockToAir(getPos());
    }
}
