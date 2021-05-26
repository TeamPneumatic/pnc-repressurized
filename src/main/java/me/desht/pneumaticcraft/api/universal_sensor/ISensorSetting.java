package me.desht.pneumaticcraft.api.universal_sensor;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

public interface ISensorSetting extends IBaseSensor {
    /**
     * Get the air usage for this sensor.
     * 
     * @return the sensor air usage in mL air per tick
     */
    default int getAirUsage(World world, BlockPos pos) {
        return 1;
    }

    /**
     * Notify the sensor that the textbox has changed, so it can carry out any necessary recalculation.
     */
    default void notifyTextChange(String newText) {
    }

    /**
     * Check if this sensor type needs a GPS (or GPS Area) Tool
     * @return true if a GPS Tool is required
     */
    default boolean needsGPSTool() {
        return false;
    }

    /**
     * Don't call this directly; used internally by {@link #getDescription()}
     * @param path the sensor path
     * @return some description text for the sensor
     */
    static List<String> _getDescription(String path) {
        String key = path.toLowerCase(Locale.ROOT).replaceAll("[/ ]", "_").replace(".", "");
        return ImmutableList.of("pneumaticcraft.gui.universalSensor.desc." + key);
    }
}
