package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.progwidgets.ICountWidget;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Direction;

public class GuiProgWidgetImportExport<P extends IProgWidget & ISidedWidget & ICountWidget> extends GuiProgWidgetAreaShow<P> {

    private WidgetTextFieldNumber textField;

    public GuiProgWidgetImportExport(P progWidget, GuiProgrammer guiProgrammer) {
        super(progWidget, guiProgrammer);
    }

    @Override
    public void init() {
        super.init();

        if (showSides()) {
            for (Direction dir : Direction.VALUES) {
                String sideName = PneumaticCraftUtils.getOrientationName(dir);
                GuiCheckBox checkBox = new GuiCheckBox(guiLeft + 4, guiTop + 30 + dir.getIndex() * 12, 0xFF404040,
                        sideName, b -> progWidget.getSides()[dir.getIndex()] = b.checked);
                checkBox.checked = progWidget.getSides()[dir.getIndex()];
                addButton(checkBox);
            }
        }

        GuiCheckBox useItemCount = new GuiCheckBox(guiLeft + 4, guiTop + (showSides() ? 115 : 30), 0xFF404040,
                I18n.format("gui.progWidget.itemFilter.useItemCount"),
                b -> progWidget.setUseCount(b.checked)
        );
        useItemCount.setTooltip("gui.progWidget.itemFilter.useItemCount.tooltip");
        useItemCount.checked = progWidget.useCount();
        addButton(useItemCount);

        textField = new WidgetTextFieldNumber(font, guiLeft + 7, guiTop + (showSides() ? 128 : 43), 50, 11);
        textField.setValue(progWidget.getCount());
        textField.setEnabled(useItemCount.checked);
        textField.func_212954_a(s -> progWidget.setCount(textField.getValue()));
        addButton(textField);
    }

    protected boolean showSides() {
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (showSides()) {
            font.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF404060);
        }
    }

}
