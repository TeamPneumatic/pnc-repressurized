package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneAIDig;
import pneumaticCraft.common.ai.IDroneBase;
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
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return setupMaxActions(new DroneAIDig(drone, (ProgWidgetAreaItemBase)widget), (IMaxActions)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.SLIME_PLANT_DAMAGE;
    }

}
