package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ProgWidgetComment extends ProgWidgetString{

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return null;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add(I18n.format("gui.progWidget.comment.tooltip.freeToUse"));
    }

    @Override
    protected boolean addToTooltip(){
        return false;
    }

    @Override
    public String getWidgetString(){
        return "comment";
    }

    @Override
    public int getHeight(){
        return super.getHeight() + 10;
    }

    @Override
    public int getWidth(){
        return super.getWidth() + 10;
    }

    @Override
    public int getCraftingColorIndex(){
        return -1;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_COMMENT;
    }

    @Override
    public String getExtraStringInfo(){
        return string;
    }

}
