package me.desht.pneumaticcraft.api.universalSensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPollSensorSetting extends ISensorSetting {

    /**
     * The value returned here is the interval between every check in ticks (the interval of calling getRedstoneValue()).
     * Consider increasing the interval when your sensor method is resource intensive.
     *
     * @param te universal sensor
     * @return
     */
    int getPollFrequency(TileEntity te);

    /**
     * The base method. This method should return the outputted redstone value 0-15 of this sensor. When this sensor is
     * digital, just return 0 or 15.
     *
     * @param world
     * @param pos
     * @param sensorRange Range of the sensor, based on the amount of Range Upgrades inserted in the Universal Sensor.
     * @param textBoxText The text typed in the textbox of the Universal Sensor.
     * @return
     */
    int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText);

}
