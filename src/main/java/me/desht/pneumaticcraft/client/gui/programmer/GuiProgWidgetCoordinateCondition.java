package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRadioButton;
import me.desht.pneumaticcraft.common.progwidgets.ICondition.Operator;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCoordinateCondition;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;

public class GuiProgWidgetCoordinateCondition extends GuiProgWidgetOptionBase<ProgWidgetCoordinateCondition> {

    public GuiProgWidgetCoordinateCondition(ProgWidgetCoordinateCondition widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        for (Direction.Axis axis : Direction.Axis.values()) {
            WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 10, guiTop + 30 + axis.ordinal() * 12, 0xFF404040,
                    new StringTextComponent(axis.getName2()), b -> progWidget.getAxisOptions().setCheck(axis, b.checked));
            addButton(checkBox);
            checkBox.setChecked(progWidget.getAxisOptions().shouldCheck(axis));
        }

        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        for (Operator op : Operator.values()) {
            builder.addRadioButton(new WidgetRadioButton(guiLeft + 80, guiTop + 30 + op.ordinal() * 12, 0xFF404040,
                            new StringTextComponent(op.toString()), b -> progWidget.setOperator(op)),
                    progWidget.getOperator() == op);
        }
        builder.build(this::addButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        String condition = progWidget.getCondition();
        font.drawString(matrixStack, condition, width / 2f - font.getStringWidth(condition) / 2f, guiTop + 70, 0xFF404060);
    }
}
