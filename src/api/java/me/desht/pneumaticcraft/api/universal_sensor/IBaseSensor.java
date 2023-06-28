/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.universal_sensor;

import me.desht.pneumaticcraft.api.misc.RangedInt;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
    Set<PNCUpgrade> getRequiredUpgrades();

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
    default RangedInt getTextboxIntRange() {
        return null;
    }

    /**
     * If this sensor should have a popup help panel, return a translation key here for the help text which should be
     * shown when F1 is held down.  The translated text can include line breaks; use the standard sequence {@code \n}
     * for that.
     * @return help text translation key, or the empty string for no help text
     */
    default String getHelpText() {
        return "";
    }

    /**
     * Only used if {@link #getHelpText()} returns a non-empty string; return a translation key for a "Hold F1" type
     * message for this sensor.
     * @return a translation key, or the empty string for no help prompt text
     */
    default String getHelpPromptText() {
        return "";
    }

    /**
     * If a textbox is to be displayed (see {@link #needsTextBox()}), this can be used to return a list of possible
     * values. If this returns a non-null result, the textbox will become a combo box with a drop-down for the
     * values returned.
     * @param player the client player
     * @return a list of possible values, or null if the textbox should be purely free-form
     */
    default List<String> getTextBoxOptions(Player player) {
        return null;
    }

    /**
     * If {@link #getTextBoxOptions(Player)} returns a non-null result, this can be used to control if the
     * returned options are the only possible values, or whether the textbox should continue to allow free-form text
     * insertion.
     * @return true for strict combo box behaviour, false to allow freeform text insertion
     */
    default boolean strictComboBox() {
        return false;
    }

    /**
     * See {@link ISensorSetting#getDescription()}
     *
     * @return
     */
    default List<String> getDescription() {
        return ISensorSetting._getDescription(getSensorPath());
    }

    /**
     * Return some descriptive text to be displayed above the optional textbox
     *
     * @param info a text component list to be appended to
     */
    default void getAdditionalInfo(List<Component> info) {}
}
