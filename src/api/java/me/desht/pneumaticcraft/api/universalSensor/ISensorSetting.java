package me.desht.pneumaticcraft.api.universalSensor;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import org.lwjgl.util.Rectangle;

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
     * Should return the required items in the upgrade slots of a Universal Sensor. This will automatically include a GPS Tool for sensors that require a location.
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
     * Not being used at the moment, I recommend returning null for now. It is going to be used to allow sensors to decide their
     * status on a item which can be inserted in a slot in the GUI if this method returns a rectangle with the coordinates of
     * the slot.
     *
     * @return
     */
    Rectangle needsSlot();
}
