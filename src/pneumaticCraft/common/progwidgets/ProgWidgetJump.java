package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.lib.Textures;

public class ProgWidgetJump extends ProgWidget{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public boolean hasStepOutput(){
        return false;
    }

    @Override
    public IProgWidget getOutputWidget(List<IProgWidget> allWidgets){
        ProgWidgetString jumpedLabel = (ProgWidgetString)getConnectedParameters()[0];
        if(jumpedLabel != null) {
            return jumpToLabel(allWidgets, jumpedLabel.string);
        }
        return null;
    }

    /**
     * Used by condition pieces
     * @param allWidgets
     * @param conditionValue
     * @return
     */
    public static IProgWidget jumpToLabel(List<IProgWidget> allWidgets, IProgWidget conditionWidget, boolean conditionValue){
        ProgWidgetString textWidget = (ProgWidgetString)(conditionValue ? conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length - 1] : conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length * 2 - 1]);
        if(textWidget != null) {
            return jumpToLabel(allWidgets, textWidget.string);
        } else {
            return conditionWidget.getOutputWidget();
        }
    }

    public static IProgWidget jumpToLabel(List<IProgWidget> allWidgets, String label){
        for(IProgWidget widget : allWidgets) {
            if(widget instanceof ProgWidgetLabel) {
                ProgWidgetString labelWidget = (ProgWidgetString)widget.getConnectedParameters()[0];
                if(labelWidget != null && labelWidget.string.equals(label)) {
                    return widget;
                }
            }
        }
        return null;
    }

    @Override
    public IProgWidget getOutputWidget(){
        return null;
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
        return "jump";
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
        return Textures.PROG_WIDGET_JUMP;
    }

}
