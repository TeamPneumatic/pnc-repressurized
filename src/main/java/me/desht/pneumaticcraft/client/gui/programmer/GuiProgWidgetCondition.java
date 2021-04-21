package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.ICondition;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCondition;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEntityCondition;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiProgWidgetCondition<T extends ProgWidgetCondition> extends GuiProgWidgetAreaShow<T> {

    private WidgetTextFieldNumber textField;

    public GuiProgWidgetCondition(T widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (isSidedWidget()) {
            for (Direction dir : DirectionUtil.VALUES) {
                ITextComponent sideName = ClientUtils.translateDirectionComponent(dir);
                WidgetCheckBox checkBox = new WidgetCheckBox(guiLeft + 8, guiTop + 30 + dir.getIndex() * 12, 0xFF404040, sideName,
                        b -> ((ISidedWidget) progWidget).getSides()[dir.getIndex()] = b.checked);
                checkBox.checked = ((ISidedWidget) progWidget).getSides()[dir.getIndex()];
                addButton(checkBox);
            }
        }

        int baseX = isSidedWidget() ? 90 : 8;
        int baseY = isUsingAndOr() ? 60 : 30;

        WidgetRadioButton radioButton;
        WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
        if (isUsingAndOr()) {
            WidgetRadioButton.Builder.create()
                    .addRadioButton(new WidgetRadioButton(guiLeft + baseX, guiTop + 30, 0xFF404040,
                                    xlate("pneumaticcraft.gui.progWidget.condition.anyBlock"),
                                    b -> progWidget.setAndFunction(false)),
                            !progWidget.isAndFunction())
                    .addRadioButton(new WidgetRadioButton(guiLeft + baseX, guiTop + 42, 0xFF404040,
                                    xlate("pneumaticcraft.gui.progWidget.condition.allBlocks"),
                                    b -> progWidget.setAndFunction(true)),
                            progWidget.isAndFunction())
                    .build(this::addButton);
        }

        if (requiresNumber()) {
            for (ICondition.Operator op : ICondition.Operator.values()) {
                radioButton = new WidgetRadioButton(guiLeft + baseX, guiTop + baseY + op.ordinal() * 12, 0xFF404040,
                        new StringTextComponent(op.toString()), b -> progWidget.setOperator(op));
                builder.addRadioButton(radioButton, progWidget.getOperator() == op);
            }
            builder.build(this::addButton);

            textField = new WidgetTextFieldNumber(font, guiLeft + baseX, guiTop + baseY + 40, 50, 11)
                    .setValue(progWidget.getRequiredCount()).setRange(0, Integer.MAX_VALUE);
            textField.setFocused2(true);
            textField.setResponder(s -> progWidget.setRequiredCount(textField.getValue()));
            addButton(textField);
        }

        WidgetLabel label = addLabel(xlate("pneumaticcraft.gui.progWidget.condition.measure"), guiLeft + 8, guiTop + 152);
        label.setTooltip(xlate("pneumaticcraft.gui.progWidget.condition.measure.tooltip"));
        WidgetComboBox measureTextField = new WidgetComboBox(font, guiLeft + label.getWidth() + 8, guiTop + 150, 80, 11);
        measureTextField.setElements(guiProgrammer.te.getAllVariables());
        measureTextField.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
        measureTextField.setText(progWidget.getMeasureVar());
        measureTextField.setResponder(progWidget::setMeasureVar);
        addButton(measureTextField);
    }

    protected boolean isSidedWidget() {
        return progWidget instanceof ISidedWidget;
    }

    protected boolean isUsingAndOr() {
        return true;
    }

    protected boolean requiresNumber() {
        return true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (isSidedWidget()) {
            font.func_243248_b(matrixStack, xlate("pneumaticcraft.gui.progWidget.inventory.accessingSides"), guiLeft + 4, guiTop + 20, 0xFF404060);
        }
        ITextComponent s = progWidget.getExtraStringInfo().get(0);
        font.func_243248_b(matrixStack, s, guiLeft + xSize / 2f - font.getStringPropertyWidth(s) / 2f, guiTop + 120, 0xFF404060);
    }

    public static class Entity extends GuiProgWidgetCondition<ProgWidgetEntityCondition> {
        public Entity(ProgWidgetEntityCondition widget, GuiProgrammer guiProgrammer) {
            super(widget, guiProgrammer);
        }

        @Override
        protected boolean isSidedWidget() {
            return false;
        }

        @Override
        protected boolean isUsingAndOr() {
            return false;
        }
    }
}
