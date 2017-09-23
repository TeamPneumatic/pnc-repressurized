package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.DroneAITeleport;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;

public class ProgWidgetTeleport extends ProgWidgetGoToLocation {
    @Override
    public String getWidgetString() {
        return "teleport";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_TELEPORT;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAITeleport((EntityDrone) drone, (ProgWidget) widget);
    }

}
