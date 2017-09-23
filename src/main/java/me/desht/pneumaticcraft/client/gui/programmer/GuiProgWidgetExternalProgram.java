package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.client.gui.GuiProgrammer;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetExternalProgram;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiProgWidgetExternalProgram extends GuiProgWidgetAreaShow<ProgWidgetExternalProgram> {

    private GuiCheckBox shareVariables;

    public GuiProgWidgetExternalProgram(ProgWidgetExternalProgram widget, GuiProgrammer guiProgrammer) {
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui() {
        super.initGui();

        shareVariables = new GuiCheckBox(-1, guiLeft + 10, guiTop + 22, 0xFF000000, I18n.format("gui.progWidget.externalProgram.shareVariables"));
        addWidget(shareVariables);
        shareVariables.setTooltip(I18n.format("gui.progWidget.externalProgram.shareVariables.tooltip"));
        shareVariables.setChecked(widget.shareVariables);
    }

    @Override
    public void keyTyped(char chr, int keyCode) throws IOException {
        if (keyCode == 1) {
            widget.shareVariables = shareVariables.checked;
        }
        super.keyTyped(chr, keyCode);
    }
}
