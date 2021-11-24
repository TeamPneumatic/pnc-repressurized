package me.desht.pneumaticcraft.api.universal_sensor;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Set;

public interface IBaseSensor {
    /**
     * Return the button path the player has to follow in which this setting is stored.
     * For instance, when the sensor should be located in player and is called speed, you should return "player/speed".
     *
     * @return a string path to the sensor
     */
    String getSensorPath();

    /**
     * Return the upgrades required by this sensor. This will automatically include a GPS Tool for sensors that require
     * a location.
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
     * For numeric textboxes (see {@link #needsTextBox()}), get the permitted numeric range.
     *
     * @return the numeric range, or null if the textbox should allow freeform text
     */
    default RangedInteger getTextboxIntRange() {
        return null;
    }

    /**
     * If a textbox is to be displyed (see {@link #needsTextBox()}), is this textbox for an entity filter?
     * @return true if this is an entity filter, false otherwise
     */
    default boolean isEntityFilter() {
        return false;
    }

    /**
     * Get some description text for this sensor
     *
     * @return a list of translation keys
     */
    default List<String> getDescription() {
        return ISensorSetting._getDescription(getSensorPath());
    }

    /**
     * Return some descriptive text to be displayed above the optional textbox
     *
     * @param info a text component list to be appended to
     */
    default void getAdditionalInfo(List<ITextComponent> info) {}
}
