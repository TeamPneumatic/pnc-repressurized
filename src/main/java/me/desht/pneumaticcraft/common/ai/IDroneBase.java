package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

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

    void addDebugEntry(String message);

    void addDebugEntry(String message, BlockPos pos);

    LogisticsManager getLogisticsManager();

    void setLogisticsManager(LogisticsManager logisticsManager);

    void playSound(SoundEvent soundEvent, SoundCategory category, float volume, float pitch);

    void addAirToDrone(int air);

    default void onVariableChanged(String varname, boolean isCoordinate) { }
}
