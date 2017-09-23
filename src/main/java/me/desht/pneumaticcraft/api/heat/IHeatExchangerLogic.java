package me.desht.pneumaticcraft.api.heat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * DO NOT IMPLEMENT THIS CLASS YOURSELF! Use PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic() !
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
     * @param exchanger
     */
    void addConnectedExchanger(IHeatExchangerLogic exchanger);

    void removeConnectedExchanger(IHeatExchangerLogic exchanger);

    /**
     * A heat exchanger starts with 295 degrees Kelvin (22 degrees Celsius) by default.
     *
     * @param temperature in degrees Kelvin
     */
    void setTemperature(double temperature);

    double getTemperature();

    /**
     * The higher the thermal resistance, the slower the heat disperses.
     *
     * @param thermalResistance By default it's 1.
     */
    void setThermalResistance(double thermalResistance);

    double getThermalResistance();

    /**
     * The higher the capacity, the more heat can be 'stored'. This means that an object with a high capacity can heat up an object with a lower
     * capacity without losing any significant amount of temperature.
     *
     * @param capacity
     */
    void setThermalCapacity(double capacity);

    double getThermalCapacity();

    void writeToNBT(NBTTagCompound tag);

    void readFromNBT(NBTTagCompound tag);

    /**
     * Adds heat (= deltaT * Thermal Capacity) to this exchanger. negative values will remove heat.
     *
     * @param amount
     */
    void addHeat(double amount);

}
