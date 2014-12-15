package pneumaticCraft.client.gui.programmer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.math.NumberUtils;

import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetInventoryBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class GuiProgWidgetImportExport extends GuiProgWidgetAreaShow{

    private GuiCheckBox useItemCount;
    private WidgetTextField textField;

    public GuiProgWidgetImportExport(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        if(showSides()) {
            for(int i = 0; i < 6; i++) {
                String sideName = PneumaticCraftUtils.getOrientationName(ForgeDirection.getOrientation(i));
                GuiCheckBox checkBox = new GuiCheckBox(i, guiLeft + 4, guiTop + 30 + i * 12, 0xFF000000, sideName);
                checkBox.checked = ((ProgWidgetInventoryBase)widget).getSides()[i];
                addWidget(checkBox);
            }
        }

        useItemCount = new GuiCheckBox(6, guiLeft + 4, guiTop + (showSides() ? 115 : 30), 0xFF000000, I18n.format("gui.progWidget.itemFilter.useItemCount"));
        useItemCount.setTooltip("gui.progWidget.itemFilter.useItemCount.tooltip");
        useItemCount.checked = ((ProgWidgetInventoryBase)widget).useCount();
        addWidget(useItemCount);
        textField = new WidgetTextField(Minecraft.getMinecraft().fontRenderer, guiLeft + 7, guiTop + (showSides() ? 128 : 43), 50, 11);
        textField.setText(((ProgWidgetInventoryBase)widget).getCount() + "");
        textField.setEnabled(useItemCount.checked);
        addWidget(textField);
    }

    protected boolean showSides(){
        return true;
    }

    @Override
    public void actionPerformed(IGuiWidget checkBox){
        if(checkBox.getID() < 6) {
            ((ProgWidgetInventoryBase)widget).getSides()[checkBox.getID()] = ((GuiCheckBox)checkBox).checked;
        } else if(checkBox.getID() == 6) {
            ((ProgWidgetInventoryBase)widget).setUseCount(((GuiCheckBox)checkBox).checked);
            textField.setEnabled(((GuiCheckBox)checkBox).checked);
        }
        super.actionPerformed(checkBox);
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){
        super.onKeyTyped(widget);
        ((ProgWidgetInventoryBase)this.widget).setCount(NumberUtils.toInt(textField.getText()));
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Accessing sides:", guiLeft + 4, guiTop + 20, 0xFF000000);
    }

}
