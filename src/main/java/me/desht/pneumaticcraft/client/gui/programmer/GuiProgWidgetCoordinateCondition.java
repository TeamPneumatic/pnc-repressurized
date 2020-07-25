package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateCondition;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiProgWidgetCoordinateCondition extends GuiProgWidgetOptionBase<ProgWidgetCoordinateCondition> {

    public GuiProgWidgetCoordinateCondition(ProgWidgetCoordinateCondition widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        for (Direction.Axis axis : Direction.Axis.values()) {
            final int idx = axis.ordinal();
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 10, guiTop + 30 + idx * 12, 0xFF404040,
                    new StringTextComponent(axis.getName2()), b -> progWidget.checkingAxis[idx] = b.checked);
            addButton(checkBox);
            checkBox.setChecked(progWidget.checkingAxis[idx]);
        }

        List<WidgetRadioButton> radioButtons = new ArrayList<>();
        for (Operator op : Operator.values()) {
            WidgetRadioButton radioButton = new WidgetRadioButton(guiLeft + 80, guiTop + 30 + op.ordinal() * 12, 0xFF404040,
                    new StringTextComponent(op.toString()), b -> progWidget.setOperator(op));
            radioButton.checked = progWidget.getOperator() == op;
            addButton(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        String condition = progWidget.getCondition();
        font.drawString(matrixStack, condition, width / 2f - font.getStringWidth(condition) / 2f, guiTop + 70, 0xFF404060);
    }
}
