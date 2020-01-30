package me.desht.pneumaticcraft.api.heat;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents a heat exchanger owned by a tile entity. Retrieve instances of this via capability lookup; you
 * can use {@link me.desht.pneumaticcraft.api.PNCCapabilities#HEAT_EXCHANGER_CAPABILITY} or get your own
 * instance with {@code @CapabilityInject}.
 * <p>
 * <strong>Do not implement this interface yourself!</strong> You can create instances of it for your tile entities
 * with {@link IHeatRegistry#makeHeatExchangerLogic()} and store those as fields in your tile entities.
 *
 * @author MineMaarten, desht
 */
public interface IHeatExchangerLogic extends INBTSerializable<CompoundNBT> {

    /**
     * Call this to tick this logic, and make the heat disperse itself.
     */
    void tick();

    /**
     * When called (preferably on tile entity load and neighbor block updates), this will add all heat
     * exchanging neighbor tile entities as connected heat exchangers (i.e. tile entities who provide the
     * {@link IHeatExchangerLogic} capability on that side).  It will also account for neighbouring blocks with heat
     * properties, like Magma or Lava.
     * <p>
     * You don't need to call this method if this heat exchanger is not connected to the outside world (e.g.
     * the connecting heat exchanger inside a Vortex Tube).
     *
     * @param world the world
     * @param pos  the position
     * @param loseHeatToAir true if heat will be lost to adjacent air blocks, false otherwise
     * @param validSides an array of sides to check for heat exchanging neighbours
     */
    void initializeAsHull(World world, BlockPos pos, boolean loseHeatToAir, Direction... validSides);

    /**
     * Initialize this heat exchanger's ambient temperature based on the given world & position.  You don't need to call
     * this method if your heat exchanger is a hull exchanger (i.e. provides an {@link IHeatExchangerLogic} object via
     * capability lookup), as hulls are automatically initialized by
     * {@link IHeatExchangerLogic#initializeAsHull(World, BlockPos, boolean, Direction...)}
     *
     * @param world the world
     * @param pos the position
     */
    void initializeAmbientTemperature(World world, BlockPos pos);

    /**
     * When called, this will connect these two heat exchangers. You should only call this on one of the two heat
     * exchangers.
     *
     * @param exchanger the other heat exchanger
     */
    void addConnectedExchanger(IHeatExchangerLogic exchanger);

    /**
     * Disconnect a connected heat exchanger.
     *
     * @param exchanger the other heat exchanger
     */
    void removeConnectedExchanger(IHeatExchangerLogic exchanger);

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
}
