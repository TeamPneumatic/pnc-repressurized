package pneumaticCraft.common.progwidgets;

import java.util.Set;

import pneumaticCraft.common.ai.DroneAIManager;

public interface IVariableWidget{
    public void setAIManager(DroneAIManager aiManager);

    public void addVariables(Set<String> variables);
}
