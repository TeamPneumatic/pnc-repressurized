package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneEntityAIInventoryImport;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetInventoryImport extends ProgWidgetInventoryBase{

    @Override
    public String getWidgetString(){
        return "inventoryImport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_INV_IM;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneEntityAIInventoryImport(drone, drone.getSpeed(), (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.RAIN_PLANT_DAMAGE;
    }
}
