package me.desht.pneumaticcraft.common.progwidgets;

import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetJump extends ProgWidget implements IJump {

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) curInfo.add(xlate("gui.progWidget.label.error.noJumpLocation"));
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public boolean hasStepOutput() {
        return false;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets) {
        ProgWidgetString jumpedLabel = (ProgWidgetString) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            drone.getAIManager().setLabel(jumpedLabel.string);
            IProgWidget widget = jumpToLabel(drone, allWidgets, jumpedLabel.string);
            if (widget != null) return widget;
        }
        drone.addDebugEntry("gui.progWidget.jump.nowhereToJump");
        return null;
    }

    static IProgWidget jumpToLabel(IDroneBase drone, List<IProgWidget> allWidgets, IProgWidget conditionWidget, boolean conditionValue) {
        ProgWidgetString textWidget = (ProgWidgetString) (conditionValue ? conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length - 1] : conditionWidget.getConnectedParameters()[conditionWidget.getParameters().length * 2 - 1]);
        if (textWidget != null) {
            return jumpToLabel(drone, allWidgets, textWidget.string);
        } else {
            IProgWidget widget = conditionWidget.getOutputWidget();
            if (widget == null) drone.addDebugEntry("gui.progWidget.jump.nowhereToJump");
            return widget;
        }
    }

    static IProgWidget jumpToLabel(IDroneBase drone, List<IProgWidget> allWidgets, String label) {
        drone.getAIManager().setLabel(label);
        List<IProgWidget> possibleJumpLocations = new ArrayList<>();
        for (IProgWidget widget : allWidgets) {
            if (widget instanceof ILabel) {
                String labelLabel = ((ILabel) widget).getLabel();
                if (labelLabel != null && labelLabel.equals(label)) {
                    possibleJumpLocations.add(widget);
                }
            }
        }
        if (possibleJumpLocations.size() == 0) {
            drone.addDebugEntry("gui.progWidget.jump.nowhereToJump");
            return null;
        } else {
            return possibleJumpLocations.get(new Random().nextInt(possibleJumpLocations.size()));
        }

    }

    @Override
    public IProgWidget getOutputWidget() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget> returnType() {
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters() {
        return new Class[]{ProgWidgetString.class};
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
    }

    @Override
    public String getWidgetString() {
        return "jump";
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_JUMP;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.MEDIUM;
    }

    @Override
    public List<String> getPossibleJumpLocations() {
        ProgWidgetString jumpedLabel = (ProgWidgetString) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            return Collections.singletonList(jumpedLabel.string);
        }
        return null;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
