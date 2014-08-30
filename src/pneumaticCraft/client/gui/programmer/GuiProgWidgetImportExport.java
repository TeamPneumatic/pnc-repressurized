package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryBase;

public class GuiProgWidgetImportExport extends GuiProgWidgetOptionBase{

    public GuiProgWidgetImportExport(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        buttonList.add(new GuiButton(0, guiLeft + 50, guiTop + 150, 80, 20, "Show area"));

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
    public void actionPerformed(GuiButton button){
        guiProgrammer.te.previewArea(widget.getX(), widget.getY());
        //PacketDispatcher.sendPacketToServer(PacketHandlerPneumaticCraft.showDroneArea(guiProgrammer.te.xCoord, guiProgrammer.te.yCoord, guiProgrammer.te.zCoord, widget.getX(), widget.getY()));
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF000000);
    }

}
