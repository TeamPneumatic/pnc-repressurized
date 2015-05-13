package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.drone.IDrone;
import pneumaticCraft.common.ai.DroneAIBlockInteract;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetBlockRightClick extends ProgWidgetPlace{

    @Override
    public String getWidgetString(){
        return "blockRightClick";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_BLOCK_RIGHT_CLICK;
    }

    @Override
    public EntityAIBase getWidgetAI(IDrone drone, IProgWidget widget){
        return new DroneAIBlockInteract(drone, drone.getSpeed(), (ProgWidgetAreaItemBase)widget);
    }

}
