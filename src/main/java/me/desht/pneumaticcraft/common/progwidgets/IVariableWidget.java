package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAIManager;

import java.util.Set;

public interface IVariableWidget {
    void setAIManager(DroneAIManager aiManager);

    void addVariables(Set<String> variables);
}
