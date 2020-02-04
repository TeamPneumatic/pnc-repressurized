package me.desht.pneumaticcraft.common.progwidgets;

import javax.annotation.Nonnull;
import java.util.List;

public interface IJump {
    @Nonnull
    List<String> getPossibleJumpLocations();
}
