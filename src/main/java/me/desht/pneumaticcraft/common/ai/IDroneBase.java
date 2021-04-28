package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.debug.DroneDebugger;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.util.fakeplayer.DroneItemHandler;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;

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

    void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch);

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

    ITextComponent getDroneName();

    boolean isDroneStillValid();

    boolean canMoveIntoFluid(Fluid fluid);

    DroneItemHandler getDroneItemHandler();
}
