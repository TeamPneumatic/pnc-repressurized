/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.tileentity;

import it.unimi.dsi.fastutil.floats.FloatPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * An extended air handler which is used by tile entities.  It supports the concept of connected neighbouring
 * air handlers, and will push air to neighbouring air handlers with lower pressure.  It will also explode if
 * over-pressurized.
 * <p>
 * Don't implement this class yourself!  Use one of the methods in {@link IAirHandlerMachineFactory} to obtain
 * a suitable implementation for your block entity.
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
     * Set the pressure of this handler directly.  This is generally only used for creative-type tile entities, to
     * maintain a constant pressure; in nearly all cases, the {@link #addAir(int)} method should be used.
     * @param newPressure the new pressure, in bar
     */
    void setPressure(float newPressure);

    /**
     * Should be called by the owning block entity when its volume upgrades change.  This will cause the air handler
     * to recalculate its current volume.  A decrease in volume will cause air to be lost, keeping the pressure
     * constant.  An increase in volume will keep the air constant, causing a pressure drop.
     *
     * @param newVolumeUpgrades new number of volume upgrades
     */
    void setVolumeUpgrades(int newVolumeUpgrades);

    /**
     * Allow this air handler to vent air when over-pressure.
     *
     * @param pressureCheck a predicate to test if venting is required
     * @param dir direction to leak air in
     */
    void enableSafetyVenting(FloatPredicate pressureCheck, Direction dir);

    /**
     * Disallow any safety venting.
     */
    void disableSafetyVenting();

    /**
     * Must be called every tick by the owning block entity. This is called on both server and client (client needs to
     * handle sounds and particles if the air handler is leaking).
     *
     * @param ownerTE the owning block entity
     */
    void tick(BlockEntity ownerTE);

    /**
     * Mark a face of the air handler as leaking or otherwise.  When called server-side, changes will be automatically
     * synced to clients on the next tick so that particles and sound effects can be played there.  The amount of air
     * leaked in a tick is pressure-dependent; the exact amount is {@code pressure * 40 + 20 mL}.
     * <p>(Note: in theory, an air handler could leak in multiple directions at once, but this is a simplified
     * implementation to keep the code straightforward &amp; efficient, while still being effective)</p>
     *
     * @param dir the direction the leak is occurring (affects particle velocities), or null for no leak
     */
    void setSideLeaking(@Nullable Direction dir);

    /**
     * Get the side which is leaking air, if any.
     * @return the leaking side, or null if there's no leak
     */
    @Nullable
    Direction getSideLeaking();

    /**
     * Get a list of all air handlers connected to this one.
     *
     * @param ownerTE the owning block entity
     * @return a list of all connected air handlers
     */
    List<IAirHandlerMachine.Connection> getConnectedAirHandlers(BlockEntity ownerTE);

    /**
     * Set the connectable faces of this air handler. This should be called on the first server tick, and when
     * neighbouring blocks change (i.e. via {@link net.minecraft.world.level.block.Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)})
     * <p>
     * This also invalidates any cached neighbour data.
     *
     * @param sides the sides of the machine on which this air handler should be offered as a capability
     */
    void setConnectableFaces(Collection<Direction> sides);

    Tag serializeNBT();

    void deserializeNBT(CompoundTag tag);

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
         * it is handled by {@link IAirHandlerMachine#tick(BlockEntity)}
         * @param maxDispersion the maximum dispersal allowed
         */
        void setMaxDispersion(int maxDispersion);

        /**
         * Get the amount of air has been dispersed along this connection in this tick.  Note that this will be 0 until
         * calculated during {@link IAirHandlerMachine#tick(BlockEntity)}.
         *
         * @return the air which has been dispersed this tick.
         */
        int getDispersedAir();

        /**
         * Set the air which will be dispersed along this connection in this tick. You should not normally call this
         * directly; it is handled by {@link IAirHandlerMachine#tick(BlockEntity)}.
         * @param toDisperse the air which will be dispersed this tick
         */
        void setAirToDisperse(int toDisperse);
    }
}
