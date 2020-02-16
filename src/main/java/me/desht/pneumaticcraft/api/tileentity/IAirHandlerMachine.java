package me.desht.pneumaticcraft.api.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An extended air handler which is used by tile entities.  It supports the concept of connected neighbouring
 * air handlers, and will push air to neighbouring air handlers with lower pressure.  It will also explode if
 * over-pressurized.
 * <p>
 * Don't implement this class yourself!  Use one of the methods in {@link IAirHandlerMachineFactory} to obtain
 * a suitable implementation for your tile entity.
 */
public interface IAirHandlerMachine extends IAirHandler, IManoMeasurable {
    /**
     * Get the "danger" pressure level.  If air is added to the handler and the pressure level is above the danger
     * level, there is a chance of explosion, which increases as the pressure increases.
     * @return the danger pressure level, in bar
     */
    float getDangerPressure();

    /**
     * Get the "critical" pressure level, which is the hard maximum pressure for this handler.  If air is added and
     * the pressure is at or above this level, an explosion is inevitable!
     * @return the critical pressure level, in bar
     */
    float getCriticalPressure();

    /**
     * Set the pressure of this handler directly.  This is generally used for creative-type tile entities, to
     * maintain a constant pressure.
     * @param newPressure the new pressure, in bar
     */
    void setPressure(float newPressure);

    /**
     * Should be called by the owning tile entity when its volume upgrades change.  This will cause the air handler
     * to recalculate its current volume.  A decrease in volume will cause air to be lost, keeping the pressure
     * constant.  An increase in volume will keep the air constant, causing a pressure drop.
     *
     * @param newVolumeUpgrades new number of volume upgrades
     */
    void setVolumeUpgrades(int newVolumeUpgrades);

    /**
     * Should be called by the owning tile entity when its security upgrades change. A Security Upgrade will cause
     * the air handler to leak air instead of exploding.
     *
     * @param hasSecurityUpgrade true if the holder has one or more security upgrades
     */
    void setHasSecurityUpgrade(boolean hasSecurityUpgrade);

    /**
     * Must be called every tick by the owning tile entity.
     *
     * @param ownerTE the owning tile entity
     */
    void tick(TileEntity ownerTE);

    /**
     * Leak air in the given direction.  The amount of air lost is dependent on the handler's current pressure;
     * the exact amount is 20 + abs(pressure * 40) mL.
     *
     * @param ownerTE the owning tile entity
     * @param dir the direction to leak in
     */
    void airLeak(TileEntity ownerTE, Direction dir);

    /**
     * Get a list of all air handlers connected to this one.
     *
     * @param ownerTE the owning tile entity
     * @return a list of all connected air handlers
     */
    List<IAirHandlerMachine.Connection> getConnectedAirHandlers(TileEntity ownerTE);

    /**
     * Override this air handler's base volume (the volume with no upgrades installed).  Used for example by the
     * Pressure Chamber, where the base volume is dependent on the multiblock size.
     *
     * @param baseVolume the new base volume
     */
    void setBaseVolume(int baseVolume);

    INBT serializeNBT();

    void deserializeNBT(CompoundNBT compound);

    /**
     * Set the connected faces of this air handler. This should be called on the first server tick, and when
     * neighbouring blocks change (i.e. via {@link net.minecraft.block.Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)}.
     * <p>
     * This also invalidates any cached neighbour data.
     *
     * @param sides a list of sides on which this air handler should be offered as a capability
     */
    void setConnectedFaces(List<Direction> sides);

    /**
     * Represents a connection to a neighbouring air handler.
     */
    interface Connection {
        /**
         * Get the neighbouring air handler
         *
         * @return a machine air handler
         */
        IAirHandlerMachine getAirHandler();

        /**
         * Get the direction of this connection. This may be null if this is a "special" connection, created by
         * {@link IAirListener#addConnectedPneumatics(List)}
         * @return the direction of this connection if it's physically adjacent, otherwise null
         */
        @Nullable
        Direction getDirection();

        /**
         * Get the maximum air which may be dispersed along this connection in this tick. This can be controlled with
         * {@link IAirListener#getMaxDispersion(IAirHandlerMachine, Direction)}.
         * @return the maximum dispersal allowed
         */
        int getMaxDispersion();

        /**
         * Set the max air which may be dispersed along this connection. You should not normally call this directly;
         * it is handled by {@link IAirHandlerMachine#tick(TileEntity)}
         * @param maxDispersion the maximum dispersal allowed
         */
        void setMaxDispersion(int maxDispersion);

        /**
         * Get the amount of air has been dispersed along this connection in this tick.  Note that this will be 0 until
         * calculated during {@link IAirHandlerMachine#tick(TileEntity)}.
         *
         * @return the air which has been dispersed this tick.
         */
        int getDispersedAir();

        /**
         * Set the air which will be dispersed along this connection in this tick. You should not normally call this directly;
         * it is handled by {@link IAirHandlerMachine#tick(TileEntity)}.
         * @param toDisperse the air which will be dispersed this tick
         */
        void setAirToDisperse(int toDisperse);
    }
}
