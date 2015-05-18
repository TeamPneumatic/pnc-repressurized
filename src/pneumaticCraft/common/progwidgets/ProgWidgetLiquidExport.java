package pneumaticCraft.common.progwidgets;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.common.ai.DroneAILiquidExport;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLiquidExport extends ProgWidgetInventoryBase implements ILiquidFiltered{

    @Override
    public String getWidgetString(){
        return "liquidExport";
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LIQUID_EX;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetLiquidFilter.class};
    }

    @Override
    public boolean isFluidValid(Fluid fluid){
        return ProgWidgetLiquidFilter.isLiquidValid(fluid, this, 1);
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return new DroneAILiquidExport(drone, (ProgWidgetAreaItemBase)widget);
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.PROPULSION_PLANT_DAMAGE;
    }
}
