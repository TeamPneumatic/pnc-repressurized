package pneumaticCraft.client.gui.programmer;

import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class GuiProgWidgetImportExport extends GuiProgWidgetAreaShow{

    public GuiProgWidgetImportExport(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        for(int i = 0; i < 6; i++) {
            String sideName = PneumaticCraftUtils.getOrientationName(ForgeDirection.getOrientation(i));
            GuiCheckBox checkBox = new GuiCheckBox(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, sideName);
            checkBox.checked = ((ProgWidgetInventoryBase)widget).accessingSides[i];
            addWidget(checkBox);
        }
    }

    @Override
    public void actionPerformed(IGuiWidget checkBox){
        ((ProgWidgetInventoryBase)widget).accessingSides[checkBox.getID()] = ((GuiCheckBox)checkBox).checked;
        super.actionPerformed(checkBox);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF000000);
    }

}
