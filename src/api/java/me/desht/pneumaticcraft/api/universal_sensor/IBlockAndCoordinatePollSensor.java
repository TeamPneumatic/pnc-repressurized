package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

public interface IBlockAndCoordinatePollSensor extends IBaseSensor {
    /**
     * Similar to {@link IPollSensorSetting#getRedstoneValue(World, BlockPos, int, String)}, but this has the GPS tracked
     * coordinates as extra parameters. This method will only invoke with a valid GPS tool, and when all the coordinates
     * are within range.
     *
     * @param world the sensor's world
     * @param pos the sensor's position
     * @param sensorRange the sensor's current range, based on installed range upgrades
     * @param textBoxText text in the sensor GUI's textbox (may be an empty string)
     * @param positions   When only one GPS Tool is inserted this contains the position of just that tool. If two GPS Tools are inserted, These are both corners of a box, and every coordinate in this box is added to the positions argument.
     * @return the redstone signal level to be emitted
     */
    int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText, Set<BlockPos> positions);

    /**
     * See {@link IPollSensorSetting#getPollFrequency(TileEntity)}
     *
     * @return a poll frequency, in ticks
     */
    int getPollFrequency();
}
