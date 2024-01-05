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

package me.desht.pneumaticcraft.api.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Represents a heat exchanger owned by a block entity. Retrieve instances of this via capability lookup; you
 * can use {@link me.desht.pneumaticcraft.api.PNCCapabilities#HEAT_EXCHANGER_CAPABILITY} or get your own
 * instance with {@link net.minecraftforge.common.capabilities.CapabilityManager#get(CapabilityToken)}.
 * <p>
 * If you are implementing a block entity with a heat exchanger, you should <strong>not</strong> implement this
 * interface yourself; get an instance of it via {@link IHeatRegistry#makeHeatExchangerLogic()}, store it as field
 * in your BE, and provide it via capability as described above. Your BE should also call {@link #tick()} and
 * {@link #initializeAsHull(Level, BlockPos, BiPredicate, Direction...)} as documented in those methods.
 * <p>
 * If you want to attach this capability as an <em>adapater</em> to other mods' heat systems, see
 * {@link IHeatExchangerAdapter} and {@link IHeatExchangerAdapter.Simple} which are convenience extensions and
 * implementations of this interface.
 *
 * @author MineMaarten, desht
 */
public interface IHeatExchangerLogic extends INBTSerializable<CompoundTag> {
    /**
     * Call this to tick this logic, and make the heat disperse itself. In general this should be called each tick
     * by the owning block entity's {@code tick()} method, on the server side only.
     */
    void tick();

    /**
     * Discovers all heat exchanging neighbor block entities  (i.e. block entities who provide the
     * {@link IHeatExchangerLogic} capability on that side) and adds them as connected heat exchangers.  It also
     * accounts for neighbouring blocks with special heat properties, like Magma or Lava, and other special cases like
     * Heat Frames (which are entities).
     * <p>
     * This should be called by the owning block entity on first tick ({@link BlockEntity#onLoad()} is suitable)
     * and when neighboring blocks update
     * ({@link net.minecraft.world.level.block.Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)}.
     * <p>
     * You don't need to call this method if this heat exchanger is not connected to the outside world (e.g.
     * the internal connecting heat exchanger within a Vortex Tube).
     *
     * @param world the world
     * @param pos the blockpos of the owning block entity
     * @param blockFilter a whitelist check; can be used to exclude certain blocks, e.g. air or fluids. In most cases,
     *                    {@link #ALL_BLOCKS} can be passed here.
     * @param validSides an array of sides to check for heat exchanging neighbours
     */
    void initializeAsHull(Level world, BlockPos pos, BiPredicate<LevelAccessor,BlockPos> blockFilter, Direction... validSides);

    /**
     * Initialize this heat exchanger's ambient temperature based on the given world &amp; position.  You don't need to
     * call this method if your heat exchanger is a hull exchanger (i.e. provides an {@link IHeatExchangerLogic} object
     * via capability lookup), as such heat exchangers are automatically initialized by
     * {@link IHeatExchangerLogic#initializeAsHull(Level, BlockPos, BiPredicate, Direction...)}.
     *
     * @param world the world
     * @param pos the position
     */
    void initializeAmbientTemperature(Level world, BlockPos pos);

    /**
     * When called, this will create a thermal connection between this heat exchanger and the given one. This should
     * be used when your BE contains more than one heat exchanger and you need a thermal connection between them;
     * an example is the hot and cold ends of the Vortex Tube.
     * <p>
     * You <strong>don't</strong> need to call this method if your BE just has one heat exchanger to
     * expose to the world; in that case {@link #initializeAsHull(Level, BlockPos, BiPredicate, Direction...)} will
     * handle connecting your BE's heat exchanger to neighbouring blocks.
     * <p>
     * You should only call this method on one of the two heat exchangers being connected; a reciprocal connection
     * on the target heat exchanger will automatically be added.
     *
     * @param exchanger the other heat exchanger
     */
    default void addConnectedExchanger(IHeatExchangerLogic exchanger) {
        addConnectedExchanger(exchanger, true);
    }

    /**
     * @param exchanger the other heat exchanger
     * @param reciprocate whether the other exchanger should also add this one
     * @apiNote non-api; don't call directly
     */
    default void addConnectedExchanger(IHeatExchangerLogic exchanger, boolean reciprocate) {
    }

    /**
     * Disconnect a connected heat exchanger which was connected via {@link #addConnectedExchanger(IHeatExchangerLogic)}
     *
     * @param exchanger the other heat exchanger
     */
    default void removeConnectedExchanger(IHeatExchangerLogic exchanger) {
        removeConnectedExchanger(exchanger, true);
    }

    /**
     * @param exchanger the other heat exchanger
     * @param reciprocate whether the other exchanger should also remove this one
     * @apiNote non-api; don't call directly
     */
    default void removeConnectedExchanger(IHeatExchangerLogic exchanger, boolean reciprocate) {
    }

    /**
     * Set the temperature of this heat exchanger.  By default, heat exchangers start with a temperature equal to
     * the ambient temperature (in the case of non-hull exchangers which have not been initialized, the default
     * temperature is 300K, the Forge-defined temperature of water).
     *
     * @param temperature in degrees Kelvin
     */
    void setTemperature(double temperature);

    /**
     * Get the heat exchanger's current (precise) temperature. This should only be used on the server where precise
     * values are required; it isn't synced to clients by default for performance reasons. Use
     * {@link #getTemperatureAsInt()} there instead.
     *
     * @return the temperature
     */
    double getTemperature();

    /**
     * Get the heat exchanger's current temperature to the nearest integer.  This is sync'd to clients rather than
     * the precise floating-point temperature to avoid excessive network chatter.
     *
     * @return the temperature to the nearest integer
     */
    int getTemperatureAsInt();

    /**
     * Get the heat exchanger's ambient temperature, i.e. the temperature at which it initially starts, dependent
     * on its environment (biome and altitude).
     *
     * @return the ambient temperature
     */
    double getAmbientTemperature();

    /**
     * The higher the thermal resistance, the slower the heat disperses. The effective resistance is the sum of this
     * resistance plus the neighbour's resistance; if both exchangers have a resistance of 1, heat will equalize in
     * a single tick under normal circumstances.
     *
     * @param thermalResistance the thermal resistance; higher resistance means slower heat transfer
     */
    void setThermalResistance(double thermalResistance);

    /**
     * Get this heat exchanger's thermal resistance.  See {@link #setThermalResistance(double)} for more information
     * on thermal resistance.
     *
     * @return the thermal resistance; higher resistance means slower heat transfer
     */
    double getThermalResistance();

    /**
     * Set this heat exchanger's thermal capacity.
     * <p>
     * The higher the capacity, the more heat can be 'stored'. E.g. an object with a heat capacity of double the heat
     * capacity of another object will require twice as much heat gain or loss to adjust the temperature by the same
     * amount.
     *
     * @param capacity the thermal capacity
     */
    void setThermalCapacity(double capacity);

    /**
     * Get this heat exchanger's thermal capacity.  See {@link #setThermalCapacity(double)} for more information.
     *
     * @return the thermal capacity.
     */
    double getThermalCapacity();

    /**
     * Adds heat (= deltaT * Thermal Capacity) to this exchanger. Negative values will remove heat.
     *
     * @param amount the heat amount
     */
    void addHeat(double amount);

    /**
     * Check if this side of the heat exchanger has a thermal connection of any kind to the neighbouring block in the
     * given direction; whether to another heat exchanger, a static heat source like air, or a custom handler such as
     * a furnace or heat frame. The connection data is initialized in
     * {@link #initializeAsHull(Level, BlockPos, BiPredicate, Direction...)}.
     *
     * @param side the side to check
     * @return true if this side has a thermal connection of any kind
     */
    boolean isSideConnected(Direction side);

    /**
     * Register a listener which will be called if the temperature of this heat exchanger changes. This is ignored
     * for heat exchangers with constant temperature (i.e. ambient temperatures or non-block-entity blocks).
     *
     * @param listener a listener which receives the new temperature
     */
    default void addTemperatureListener(@NotNull TemperatureListener listener) {
    }

    /**
     * Removed a registered temperture listener. This should be called when the listening object goes out of scope.
     *
     * @param listener the listener to remove
     */
    default void removeTemperatureListener(@NotNull TemperatureListener listener) {
    }

    @Override
    default CompoundTag serializeNBT() { return new CompoundTag(); }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
    }

    /**
     * Get the {@link HeatBehaviour} at the given position, which must be adjacent to this heat exchanger's owning tile
     * entity, and in this heat exchanger's list of heat behaviours that it handles.
     * @param pos position of the heat behaviour
     * @param cls required class of the heat behaviour (any heat behaviour which extends this class will match)
     * @param <T> the heat behaviour type
     * @return an optional heat behaviour, or {@code Optional.empty()} if the position is invalid or there is no
     * matching heat behaviour there
     */
    default <T extends HeatBehaviour> Optional<T> getHeatBehaviour(BlockPos pos, Class<T> cls) {
        return Optional.empty();
    }

    BiPredicate<LevelAccessor,BlockPos> ALL_BLOCKS = (world, pos) -> true;

}
