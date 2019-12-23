package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * Implement this interface on your tile entities which can handle air.
 */
public interface IPneumaticMachine {
    /**
     * In your TileEntity class which is implementing this interface you should keep a reference of an IAirHandler,
     * which you can get by calling {@link IAirHandlerSupplier#createAirHandler(float, float, int)}.  You can do this
     * in the TileEntity constructor.
     * <p>
     * <strong>IMPORTANT</strong>: Your tile entity must implement {@link net.minecraft.tileentity.ITickableTileEntity}, and you must override {@link net.minecraft.tileentity.ITickableTileEntity#tick()},
     * {@link net.minecraft.tileentity.TileEntity#write(CompoundNBT)} ,
     * {@link net.minecraft.tileentity.TileEntity#read(CompoundNBT)} and
     * {@link net.minecraft.tileentity.TileEntity#validate()} (with the implementing TileEntity as additional parameter)
     * to also call the corresponding IAirHandler methods.
     * <p>
     * In addition, you must override {@link net.minecraft.block.Block#onNeighborChange(BlockState, IWorldReader, BlockPos, BlockPos)}
     * in the tile entity's block to call {@link IAirHandlerMachine#onNeighborChange()}.
     *
     * @return a valid IAirHandler when connectable on this side, null otherwise
     */
    IAirHandlerMachine getAirHandler(Direction side);

    /**
     * Convenience method to cast a TE to a IPneumaticMachine
     * @param te the tile entity
     * @return an IPneumaticMachine, or null
     */
    static IPneumaticMachine getMachine(TileEntity te) {
        return te instanceof IPneumaticMachine ? (IPneumaticMachine) te : null;
    }
}
