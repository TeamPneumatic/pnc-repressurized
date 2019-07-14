package me.desht.pneumaticcraft.api.universal_sensor;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public interface ISensorSetting {
    /**
     * Should return the button path the player has to follow in which this setting is stored.
     * For instance, when the sensor should be located in player and is called speed, you should return "player/speed".
     *
     * @return
     */
    String getSensorPath();

    /**
     * Should return the required items in the upgrade slots of a Universal Sensor. This will automatically include a
     * GPS Tool for sensors that require a location.
     *
     * @return
     */
    Set<Item> getRequiredUpgrades();

    /**
     * When returned true, the GUI will enable the textbox writing, otherwise not.
     *
     * @return
     */
    boolean needsTextBox();

    /**
     * Called by GuiScreen#drawScreen this method can be used to render additional things like status/info text.
     *
     * @param fontRenderer
     */
    void drawAdditionalInfo(FontRenderer fontRenderer);

    /**
     * Should return the description of this sensor displayed in the GUI stat. Information should at least include
     * when this sensor emits redstone and how (analog (1 through 15), or digital).
     *
     * @return
     */
    List<String> getDescription();

    /**
     * Get the air usage (per tick) for this sensor.  Default is 1mL/tick.
     * @return
     */
    default int getAirUsage(World world, BlockPos pos) {
        return 1;
    }

    /**
     * Notify the sensor that the textbox has changed, so it can carry out any necessary recalculation.
     */
    default void notifyTextChange(String newText) {
    }
}
