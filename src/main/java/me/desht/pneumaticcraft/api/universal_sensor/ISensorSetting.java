package me.desht.pneumaticcraft.api.universal_sensor;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public interface ISensorSetting {
    /**
     * Should return the button path the player has to follow in which this setting is stored.
     * For instance, when the sensor should be located in player and is called speed, you should return "player/speed".
     *
     * @return a string path to the sensor
     */
    String getSensorPath();

    /**
     * Should return the required items in the upgrade slots of a Universal Sensor. This will automatically include a
     * GPS Tool for sensors that require a location.
     *
     * @return a set of upgrades
     */
    Set<EnumUpgrade> getRequiredUpgrades();

    /**
     * Should this sensor's GUI display a text box for extra information to be entered?
     *
     * @return true if this sensor needs a text box, false otherwise
     */
    boolean needsTextBox();

    /**
     * Called by GuiScreen#drawScreen this method can be used to render additional things like status/info text.
     *
     * @param fontRenderer the font renderer
     */
    void drawAdditionalInfo(FontRenderer fontRenderer);

    /**
     * Should return the description of this sensor displayed in the GUI stat. Information should at least include
     * when this sensor emits redstone and how (analog (1 through 15), or digital).
     *
     * @return some description text for this sensor (translation keys will be auto-processed)
     */
    default List<String> getDescription() {
        return _getDescription(getSensorPath());
    }

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
        String key = path.toLowerCase().replaceAll("[/ ]", "_").replace(".", "");
        return ImmutableList.of("pneumaticcraft.gui.universalSensor.desc." + key);
    }
}
