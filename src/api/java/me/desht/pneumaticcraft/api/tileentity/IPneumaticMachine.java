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
     * In your TileEntity class which is implementing this interface you should keep a reference of an IAirHandler.
     * You can create one by calling {@link IAirHandlerSupplier#createAirHandler(float, float, int)}.
     * Do this when your TileEntity is initialized, i.e. getPos().getX(),getPos().getY(),getPos().getZ() and getWorld() have a value.
     * Return that reference from this method.
     * <p>
     * <strong>IMPORTANT</strong>: Your tile entity must implement {@link ITickable}, and you need to forward {@link ITickable#update()},
     * {@link net.minecraft.tileentity.TileEntity#writeToNBT(net.minecraft.nbt.NBTTagCompound)} ,
     * {@link net.minecraft.tileentity.TileEntity#readFromNBT(net.minecraft.nbt.NBTTagCompound)} and
     * {@link net.minecraft.tileentity.TileEntity#validate()} (with the implementing TileEntity as additional parameter)
     * to the IAirHandler.
     * <p>
     * Apart from that you'll need to forward {@link net.minecraft.block.Block#onNeighborChange(IBlockAccess, BlockPos, BlockPos)}
     * from the implementing block to the IAirHandler.
     *
     * @return a valid IAirHandler when connectable on this side. If not, return null.
     */
    IAirHandler getAirHandler(EnumFacing side);
}
