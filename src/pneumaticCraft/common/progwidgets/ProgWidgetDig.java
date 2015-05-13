package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.ai.DroneAIDig;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetDig extends ProgWidgetDigAndPlace{

    public ProgWidgetDig(){
        super(ProgWidgetDigAndPlace.EnumOrder.CLOSEST);
    }

    @Override
    public String getWidgetString(){
        return "dig";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_DIG;
    }

    @Override
    public EntityAIBase getWidgetAI(IDrone drone, IProgWidget widget){
        return new DroneAIDig(drone, 0.1, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.SLIME_PLANT_DAMAGE;
    }

}
