package me.desht.pneumaticcraft.api.heat;

import me.desht.pneumaticcraft.api.tileentity.IHeatRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * DO NOT IMPLEMENT THIS CLASS YOURSELF! Create an instance via {@link IHeatRegistry#getHeatExchangerLogic()}
 * and store it as a field in your tile entity.
 *
 * @author MineMaarten
 *         www.minemaarten.com
 */
public interface IHeatExchangerLogic {

    /**
     * Call this to tick this logic, and make the heat disperse itself.
     */
    void update();

    /**
     * When called (preferably on tile entity load and neighbor block/tile entity change) this will add all IHeatExchanger
     * neighbor TileEntities as connected heat exchangers.  It will also take care of blocks like Lava.
     * <p>
     * You don't _have_ to call this method if this heat exchanger is not connected to the outside world (for example
     * the heat of the liquid plastic in the Plastic Mixer).
     *
     * @param world
     * @param pos
     * @param validSides Can be left out as vararg, meaning every side can be connected. When one or more sides are specified this will constrain
     *                   this heat exchanger to only connect to other heat exchangers on these sides.
     */
    void initializeAsHull(World world, BlockPos pos, EnumFacing... validSides);

    /**
     * When called, this will connect these two heat exchangers. You should only call this on one of the two heat exchangers.
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
     * A heat exchanger starts with 295 degrees Kelvin (22 degrees Celsius) by default.
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
     * @param thermalResistance the thermal resistance, higher resistance means slower heat transfer
     */
    void setThermalResistance(double thermalResistance);

    /**
     * Get this heat exchanger's thermal resistance.  See {@link #setThermalResistance(double)} for more information
     * on thermal resistance.
     *
     * @return the thermal resistance, higher resistance means slower heat transfer
     */
    double getThermalResistance();

    /**
     * Set this heat exchanger's thermal capacity.
     * <p>
     * The higher the capacity, the more heat can be 'stored'. This means that an object with a high capacity can heat
     * up an object with a lower capacity without losing any significant amount of temperature.
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

    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    /**
     * Adds heat (= deltaT * Thermal Capacity) to this exchanger. Negative values will remove heat.
     *
     * @param amount the heat amount
     */
    void addHeat(double amount);
}
