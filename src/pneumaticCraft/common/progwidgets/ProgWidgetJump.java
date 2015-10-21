package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.util.ResourceLocation;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;

public class ProgWidgetJump extends ProgWidget implements IJump{

    @Override
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(getConnectedParameters()[0] == null) curInfo.add("gui.progWidget.label.error.noJumpLocation");
    }

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public boolean hasStepOutput(){
        return false;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        ProgWidgetString jumpedLabel = (ProgWidgetString)getConnectedParameters()[0];
        if(jumpedLabel != null) {
            drone.getAIManager().setLabel(jumpedLabel.string);
            return jumpToLabel(allWidgets, jumpedLabel.string);
        }
        return null;
    }

    /**
     * Used by condition pieces
     * @param drone TODO
     * @param allWidgets
     * @param conditionValue
     * @return
     */
    public static IProgWidget jumpToLabel(IDroneBase drone, List<IProgWidget> allWidgets, IProgWidget conditionWidget, boolean conditionValue){
        ProgWidgetString textWidget = (ProgWidgetString)(conditionValue ? conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length - 1] : conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length * 2 - 1]);
        if(textWidget != null) {
            drone.getAIManager().setLabel(textWidget.string);
            return jumpToLabel(allWidgets, textWidget.string);
        } else {
            return conditionWidget.getOutputWidget();
        }
    }

    public static IProgWidget jumpToLabel(List<IProgWidget> allWidgets, String label){
        List<IProgWidget> possibleJumpLocations = new ArrayList<IProgWidget>();
        for(IProgWidget widget : allWidgets) {
            if(widget instanceof ILabel) {
                String labelLabel = ((ILabel)widget).getLabel();
                if(labelLabel != null && labelLabel.equals(label)) {
                    possibleJumpLocations.add(widget);
                }
            }
        }
        return possibleJumpLocations.size() == 0 ? null : possibleJumpLocations.get(new Random().nextInt(possibleJumpLocations.size()));
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
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_JUMP;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.MEDIUM;
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
