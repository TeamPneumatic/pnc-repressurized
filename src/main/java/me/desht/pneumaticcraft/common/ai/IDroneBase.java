package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IDroneBase extends IDrone {

    List<IProgWidget> getProgWidgets();

    void setActiveProgram(IProgWidget widget);

    boolean isProgramApplicable(IProgWidget widget);

    void overload();

    DroneAIManager getAIManager();

    /**
     * Sets the label that was jumped to last, with a hierarchy in case of External Programs.
     */
    void updateLabel();

    void addDebugEntry(String message);

    void addDebugEntry(String message, BlockPos pos);
}
