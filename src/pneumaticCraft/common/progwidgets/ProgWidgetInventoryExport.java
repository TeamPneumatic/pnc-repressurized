package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneEntityAIInventoryExport;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;

public class ProgWidgetInventoryExport extends ProgWidgetInventoryBase{
    @Override
    public String getWidgetString(){
        return "inventoryExport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_INV_EX;
    }

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneEntityAIInventoryExport(drone, 0.1, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public String getGuiTabText(){
        return "With this the Drone will export items from the Drone's inventory to an inventory located within the selected area. You can filter which items are allowed to be exported by using item filters. \n \nAir usage: " + PneumaticValues.DRONE_USAGE_INV + "mL/stack.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFe16a00;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }
}
