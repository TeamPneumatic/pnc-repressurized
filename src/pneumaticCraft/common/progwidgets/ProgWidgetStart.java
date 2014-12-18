package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetStart extends ProgWidget{

    @Override
    public boolean hasStepInput(){
        return false;
    }

    @Override
    public boolean hasStepOutput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return null;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_START;
    }

    @Override
    public String getWidgetString(){
        return "start";
    }

    @Override
    public String getGuiTabText(){
        return "This module is used as start of every program. It's the first block in any sequence.";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFF6cbc37;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.FLOW_CONTROL;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.REPULSION_PLANT_DAMAGE;
    }
}
