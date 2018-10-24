package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Implement this interface on your tile entities which can handle air.
 */
public interface IPneumaticMachine {
    /**
     * In your TileEntity class which is implementing this interface you should keep a reference of an IAirHandler,
     * which you can get by calling {@link IAirHandlerSupplier#createAirHandler(float, float, int)}.  You can do this
     * in the TileEntity constructor.
     * <p>
     * <strong>IMPORTANT</strong>: Your tile entity must implement {@link ITickable}, and you must override {@link ITickable#update()},
     * {@link net.minecraft.tileentity.TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)} ,
     * {@link net.minecraft.tileentity.TileEntity#readFromNBT(net.minecraft.nbt.NBTTagCompound)} and
     * {@link net.minecraft.tileentity.TileEntity#validate()} (with the implementing TileEntity as additional parameter)
     * to also call the corresponding IAirHandler methods.
     * <p>
     * In addition, you must override {@link net.minecraft.block.Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)}
     * in the tile entity's block to call {@link IAirHandler#onNeighborChange()}.
     *
     * @return a valid IAirHandler when connectable on this side, null otherwise
     */
    IAirHandler getAirHandler(EnumFacing side);
}
