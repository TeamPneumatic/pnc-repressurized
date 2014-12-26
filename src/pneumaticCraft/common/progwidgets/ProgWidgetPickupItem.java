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
    public String getGuiTabText(){
        return "This program is used to make the Drone pick up items that are lying on the ground within the selected area. Note that the connected 'Area' puzzle pieces always will be handled as they were in 'Filled' mode.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFff7cf7;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.POTION_PLANT_DAMAGE;
    }
}
