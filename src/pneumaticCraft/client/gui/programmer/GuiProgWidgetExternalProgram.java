package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.resources.I18n;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.common.progwidgets.ProgWidgetExternalProgram;

public class GuiProgWidgetExternalProgram extends GuiProgWidgetAreaShow<ProgWidgetExternalProgram>{

    private GuiCheckBox shareVariables;

    public GuiProgWidgetExternalProgram(ProgWidgetExternalProgram widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        shareVariables = new GuiCheckBox(-1, guiLeft + 10, guiTop + 22, 0xFF000000, I18n.format("gui.progWidget.externalProgram.shareVariables"));
        addWidget(shareVariables);
        shareVariables.setTooltip(I18n.format("gui.progWidget.externalProgram.shareVariables.tooltip"));
        shareVariables.setChecked(widget.shareVariables);
    }

    @Override
    public void keyTyped(char chr, int keyCode){
        if(keyCode == 1) {
            widget.shareVariables = shareVariables.checked;
        }
        super.keyTyped(chr, keyCode);
    }
}
