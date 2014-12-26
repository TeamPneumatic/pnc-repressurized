package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneEntityAIInventoryImport;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.PneumaticValues;
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
    public String getGuiTabText(){
        return "With this the Drone will import items from an inventory located within the selected area to the Drone's inventory. You can filter which items are allowed to be imported by using item filters. \n \nAir usage: " + PneumaticValues.DRONE_USAGE_INV + "mL/stack.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF0000ff;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.RAIN_PLANT_DAMAGE;
    }
}
