package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneAIDig;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.PneumaticValues;
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
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        return new DroneAIDig(drone, 0.1, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public String getGuiTabText(){
        return "This program is used to allow the Drone to dig blocks within the selected area. You can also specify which blocks are allowed to be dug by using item filters. Not every block can be put in the item filter (like crops). Use the item that is dropped from that block instead. \n \nIt's possible to equip the Drone with a tool which can be used to allow the Drone to dig faster, or to apply effects like Fortune. \n \nAir usage: " + PneumaticValues.DRONE_USAGE_DIG + "mL/block.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF8e3900;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.SLIME_PLANT_DAMAGE;
    }

}
