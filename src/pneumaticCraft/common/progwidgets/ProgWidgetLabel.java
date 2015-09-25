package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLabel extends ProgWidget implements ILabel{

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(getConnectedParameters()[0] == null) curInfo.add("gui.progWidget.label.error.noLabel");
    }

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
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_LABEL;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.MEDIUM;
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
