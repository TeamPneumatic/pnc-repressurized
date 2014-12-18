package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLabel extends ProgWidget implements ILabel{

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
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist(){
        return false;
    }

    @Override
    public String getWidgetString(){
        return "label";
    }

    @Override
    public String getGuiTabText(){
        return "bla";
    }

    @Override
    public int getGuiTabColor(){
        return 0xFFFFFFFF;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LABEL;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.FLOW_CONTROL;
    }

    @Override
    public String getLabel(){
        ProgWidgetString labelWidget = (ProgWidgetString)getConnectedParameters()[0];
        return labelWidget != null ? labelWidget.string : null;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FLYING_FLOWER_DAMAGE;
    }
}
