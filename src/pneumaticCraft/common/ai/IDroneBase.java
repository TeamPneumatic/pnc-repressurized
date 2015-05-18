package pneumaticCraft.common.ai;

import java.util.List;

import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;

public interface IDroneBase extends IDrone{

    public List<IProgWidget> getProgWidgets();

    public void setActiveProgram(IProgWidget widget);

    public boolean isProgramApplicable(IProgWidget widget);

}
