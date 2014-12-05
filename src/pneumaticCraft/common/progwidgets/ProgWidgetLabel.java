package pneumaticCraft.common.progwidgets;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ProgWidgetLabel extends ProgWidget{

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

}
