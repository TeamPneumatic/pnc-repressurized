package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public interface IPollSensorSetting extends ISensorSetting {
    /**
     * The value returned here is the interval between every check in ticks (i.e. how frequently
     * {@link #getRedstoneValue(World, BlockPos, int, String)} should be called.
     * Consider increasing the interval when that method is resource-intensive.
     *
     * @param te universal sensor
     * @return the interval in ticks between polling operations
     */
    int getPollFrequency(TileEntity te);

    /**
     * Called regularly by the Universal Sensor tile entity to calculate the output redstone value 0-15 of this sensor.
     * When this sensor is digital, just return 0 or 15.
     *
     * @param world the world
     * @param pos the blockpos to test
     * @param sensorRange range of the sensor, based on the number of Range Upgrades inserted in the Universal Sensor.
     * @param textBoxText any text typed in the textfield of the Universal Sensor GUI.
     * @return level of the redstone signal that the Universal Sensor block should emit
     */
    int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText);

    /**
     * Called immediately before {@link #getRedstoneValue(World, BlockPos, int, String)} to set up the player context,
     * if necessary. If this sensor doesn't care about player context, there's no need to override this.
     *
     * @param playerID unique ID for the player who placed down the calling Universal Sensor
     */
    default void setPlayerContext(UUID playerID) {
    }
}
