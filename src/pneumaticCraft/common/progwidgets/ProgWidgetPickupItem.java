package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneEntityAIPickupItems;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetPickupItem extends ProgWidgetAreaItemBase{

    @Override
    public String getWidgetString(){
        return "pickupItem";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_PICK_ITEM;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneEntityAIPickupItems(drone, 0.1, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.POTION_PLANT_DAMAGE;
    }
}
