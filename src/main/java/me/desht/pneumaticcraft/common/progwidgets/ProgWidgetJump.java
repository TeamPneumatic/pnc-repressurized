package me.desht.pneumaticcraft.common.progwidgets;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ProgWidgetJump extends ProgWidget implements IJump {

    public ProgWidgetJump() {
        super(ModProgWidgets.JUMP.get());
    }

    @Override
    public void addErrors(List<ITextComponent> curInfo, List<IProgWidget> widgets) {
        super.addErrors(curInfo, widgets);
        if (getConnectedParameters()[0] == null) curInfo.add(xlate("pneumaticcraft.gui.progWidget.label.error.noJumpLocation"));
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
        ProgWidgetText jumpedLabel = (ProgWidgetText) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            drone.getAIManager().setLabel(jumpedLabel.string);
            IProgWidget widget = jumpToLabel(drone, allWidgets, jumpedLabel.string);
            if (widget != null) return widget;
        }
        drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
        return null;
    }

    static IProgWidget jumpToLabel(IDroneBase drone, List<IProgWidget> allWidgets, IProgWidget conditionWidget, boolean conditionValue) {
        ProgWidgetText textWidget = (ProgWidgetText) (conditionValue ?
                conditionWidget.getConnectedParameters()[conditionWidget.getParameters().size() - 1] :
                conditionWidget.getConnectedParameters()[conditionWidget.getParameters().size() * 2 - 1]);
        if (textWidget != null) {
            return jumpToLabel(drone, allWidgets, textWidget.string);
        } else {
            IProgWidget widget = conditionWidget.getOutputWidget();
            if (widget == null) drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
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
            drone.getDebugger().addEntry("pneumaticcraft.gui.progWidget.jump.nowhereToJump");
            return null;
        } else {
            return possibleJumpLocations.get(ThreadLocalRandom.current().nextInt(possibleJumpLocations.size()));
        }

    }

    @Override
    public IProgWidget getOutputWidget() {
        return null;
    }

    @Override
    public ProgWidgetType<?> returnType() {
        return null;
    }

    @Override
    public List<ProgWidgetType<?>> getParameters() {
        return ImmutableList.of(ModProgWidgets.TEXT.get());
    }

    @Override
    protected boolean hasBlacklist() {
        return false;
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
        ProgWidgetText jumpedLabel = (ProgWidgetText) getConnectedParameters()[0];
        if (jumpedLabel != null) {
            return Collections.singletonList(jumpedLabel.string);
        }
        return Collections.emptyList();
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}
