package me.desht.pneumaticcraft.api.remote;

import java.util.Set;
import java.util.UUID;

public interface IRemoteVariableWidget extends IRemoteWidget {
    String varName();

    @Override
    default void discoverVariables(Set<String> variables, UUID playerId) {
        IRemoteWidget.super.discoverVariables(variables, playerId);

        if (!varName().isEmpty()) {
            variables.add(varName());
        }
    }
}
