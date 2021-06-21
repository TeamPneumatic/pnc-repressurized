package me.desht.pneumaticcraft.client.gui.programmer;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.progwidgets.*;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class GuiProgWidgetDroneCondition<T extends ProgWidgetDroneCondition> extends GuiProgWidgetOptionBase<T> {
    private WidgetTextFieldNumber textField;

    GuiProgWidgetDroneCondition(T progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
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

        int baseX = isSidedWidget() ? 94 : 8;
        int baseY = isUsingAndOr() ? 60 : 30;

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
            WidgetRadioButton.Builder<WidgetRadioButton> builder = WidgetRadioButton.Builder.create();
            for (ICondition.Operator op : ICondition.Operator.values()) {
                builder.addRadioButton(new WidgetRadioButton(guiLeft + baseX, guiTop + baseY + op.ordinal() * 12, 0xFF404040,
                        new StringTextComponent(op.toString()), b -> progWidget.setOperator(op)),
                        progWidget.getOperator() == op);
            }
            builder.build(this::addButton);

            textField = new WidgetTextFieldNumber(font, guiLeft + baseX, guiTop + baseY + 40, 50, 11).setRange(0, Integer.MAX_VALUE);
            textField.setText(progWidget.getRequiredCount() + "");
            textField.setMaxStringLength(GlobalVariableManager.MAX_VARIABLE_LEN);
            textField.setFocused2(true);
            textField.setResponder(s -> progWidget.setRequiredCount(textField.getValue()));
            addButton(textField);
            setFocusedDefault(textField);
        }

        WidgetLabel label = addLabel(xlate("pneumaticcraft.gui.progWidget.condition.measure"), guiLeft + 8, guiTop + 152);
        label.setTooltip(xlate("pneumaticcraft.gui.progWidget.condition.measure.tooltip"));
        WidgetComboBox measureTextField = new WidgetComboBox(font, guiLeft + label.getWidth() + 8, guiTop + 150, 80, 11);
        measureTextField.setElements(guiProgrammer.te.getAllVariables());
        measureTextField.setText(progWidget.getMeasureVar());
        measureTextField.setResponder(progWidget::setMeasureVar);
        addButton(measureTextField);
    }

    protected boolean isSidedWidget() {
        return progWidget instanceof ISidedWidget;
    }

    protected boolean isUsingAndOr() {
        return false;
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

    public static class Item extends GuiProgWidgetDroneCondition<ProgWidgetDroneConditionItem> {
        public Item(ProgWidgetDroneConditionItem progWidget, GuiProgrammer guiProgrammer) {
            super(progWidget, guiProgrammer);
        }
    }

    public static class Fluid extends GuiProgWidgetDroneCondition<ProgWidgetDroneConditionFluid> {
        public Fluid(ProgWidgetDroneConditionFluid progWidget, GuiProgrammer guiProgrammer) {
            super(progWidget, guiProgrammer);
        }
    }
    public static class Pressure extends GuiProgWidgetDroneCondition<ProgWidgetDroneConditionPressure> {
        public Pressure(ProgWidgetDroneConditionPressure progWidget, GuiProgrammer guiProgrammer) {
            super(progWidget, guiProgrammer);
        }
    }
    public static class Energy extends GuiProgWidgetDroneCondition<ProgWidgetDroneConditionEnergy> {
        public Energy(ProgWidgetDroneConditionEnergy progWidget, GuiProgrammer guiProgrammer) {
            super(progWidget, guiProgrammer);
        }
    }

    public static class Upgrades extends GuiProgWidgetDroneCondition<ProgWidgetDroneConditionUpgrades> {
        public Upgrades(ProgWidgetDroneConditionUpgrades widget, GuiProgrammer guiProgrammer) {
            super(widget, guiProgrammer);
        }
    }
}
