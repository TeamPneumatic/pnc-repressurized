package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneAILogistics;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLogistics extends ProgWidgetAreaItemBase{

    @Override
    public String getWidgetString(){
        return "logistics";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.ENDER_PLANT_DAMAGE;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LOGISTICS;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class};
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAILogistics(drone, (ProgWidgetAreaItemBase)widget);
    }

}
