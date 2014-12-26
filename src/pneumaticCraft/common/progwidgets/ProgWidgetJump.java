package pneumaticCraft.common.progwidgets;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetJump extends ProgWidget implements IJump{

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public boolean hasStepOutput(){
        return false;
    }

    @Override
    public IProgWidget getOutputWidget(EntityDrone drone, List<IProgWidget> allWidgets){
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
            if(widget instanceof ILabel) {
                String labelLabel = ((ILabel)widget).getLabel();
                if(labelLabel != null && labelLabel.equals(label)) {
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

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.FLOW_CONTROL;
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        ProgWidgetString jumpedLabel = (ProgWidgetString)getConnectedParameters()[0];
        if(jumpedLabel != null) {
            return Arrays.asList(jumpedLabel.string);
        }
        return null;
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.FLYING_FLOWER_DAMAGE;
    }
}
