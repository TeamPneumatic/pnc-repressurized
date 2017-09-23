package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetEmitRedstone;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.util.EnumFacing;

public class GuiProgWidgetEmitRedstone extends GuiProgWidgetOptionBase<ProgWidgetEmitRedstone> {

    public GuiProgWidgetEmitRedstone(ProgWidgetEmitRedstone widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < 6; i++) {
            String sideName = PneumaticCraftUtils.getOrientationName(EnumFacing.getFront(i));
            GuiCheckBox checkBox = new GuiCheckBox(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, sideName);
            checkBox.checked = widget.getSides()[i];
            addWidget(checkBox);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget checkBox) {
        if (checkBox.getID() < 6 && checkBox.getID() >= 0) {
            widget.getSides()[checkBox.getID()] = ((GuiCheckBox) checkBox).checked;
        }
        super.actionPerformed(checkBox);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("Affecting sides:", guiLeft + 4, guiTop + 20, 0xFF000000);
    }
}
