package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.DroneEntityAIInventoryExport;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
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
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneEntityAIInventoryExport(drone, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }
}
