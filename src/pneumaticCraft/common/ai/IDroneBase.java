package pneumaticCraft.common.ai;

import java.util.List;

import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;

public interface IDroneBase extends IDrone{

    public List<IProgWidget> getProgWidgets();

    public void setActiveProgram(IProgWidget widget);

    public boolean isProgramApplicable(IProgWidget widget);

    public void overload();

    public DroneAIManager getAIManager();

    /**
     * Sets the label that was jumped to last, with a hierarchy in case of External Programs.
     */
    public void updateLabel();

    public void addDebugEntry(String message);

    public void addDebugEntry(String message, ChunkPosition pos);
}
