package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.ai.DroneAITeleport;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.lib.Textures;

public class ProgWidgetTeleport extends ProgWidgetGoToLocation{
    @Override
    public String getWidgetString(){
        return "teleport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_TELEPORT;
    }

    @Override
    public EntityAIBase getWidgetAI(IDrone drone, IProgWidget widget){
        return new DroneAITeleport((EntityDrone)drone, 0.1, (ProgWidget)widget);
    }

}
