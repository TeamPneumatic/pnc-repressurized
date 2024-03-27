/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.debug.DroneDebugger;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIExternalProgram;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIManager;
import me.desht.pneumaticcraft.common.drone.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.network.DronePacket;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * Non-API extension to public IDrone interface
 */
public interface IDroneBase extends IDrone {

    List<IProgWidget> getProgWidgets();

    void setActiveProgram(IProgWidget widget);

    boolean isProgramApplicable(ProgWidgetType<?> widgetType);

    void overload(String msgKey, Object... params);

    DroneAIManager getAIManager();

    /**
     * Sets the label that was jumped to last, with a hierarchy in case of External Programs.
     */
    void updateLabel();

    LogisticsManager getLogisticsManager();

    void setLogisticsManager(LogisticsManager logisticsManager);

    void playSound(SoundEvent soundEvent, SoundSource category, float volume, float pitch);

    void addAirToDrone(int air);

    default void onVariableChanged(String varname, boolean isCoordinate) { }

    int getActiveWidgetIndex();

    DroneDebugger getDebugger();

    void storeTrackerData(ItemStack stack);

    /**
     * Get the currently-active programming widget.  Used client-side for debugging and rendering.
     *
     * @return the currently-active programming widget
     */
    default IProgWidget getActiveWidget() {
        int index = getActiveWidgetIndex();
        if (index >= 0 && index < getProgWidgets().size()) {
            return getProgWidgets().get(index);
        } else {
            return null;
        }
    }

    String getLabel();

    Component getDroneName();

    boolean isDroneStillValid();

    boolean canMoveIntoFluid(Fluid fluid);

    DroneItemHandler getDroneItemHandler();

    default void resetAttackCount() {
    }

    /**
     * Get the currently-active AI manager. Normally the drone's own aiManager object, but if currently executing
     * an External Program widget, then return the sub-AI which is in effect.
     * @return the active AI manager
     */
    default DroneAIManager getActiveAIManager() {
        return getAIManager().getCurrentGoal() instanceof DroneAIExternalProgram ext ? ext.getRunningAI() : getAIManager();
    }

    float getDronePressure();

    DronePacket.DroneTarget getPacketTarget();
}
