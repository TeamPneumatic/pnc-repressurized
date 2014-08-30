package pneumaticCraft.client.gui.programmer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiInventorySearcher;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.widget.GuiRadioButton;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketProgrammerUpdate;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetArea;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiProgWidgetArea extends GuiProgWidgetOptionBase{
    private GuiInventorySearcher invSearchGui;
    private int pointSearched;

    public GuiProgWidgetArea(IProgWidget widget, GuiProgrammer guiProgrammer){
        super(widget, guiProgrammer);
    }

    @Override
    public void initGui(){
        super.initGui();

        buttonList.add(new GuiButton(0, guiLeft + 4, guiTop + 20, 86, 20, "Select GPS 1..."));
        buttonList.add(new GuiButton(1, guiLeft + 93, guiTop + 20, 86, 20, "Select GPS 2..."));
        buttonList.add(new GuiButton(2, guiLeft + 50, guiTop + 150, 80, 20, "Show area"));

        List<GuiRadioButton> radioButtons = new ArrayList<GuiRadioButton>();
        ProgWidgetArea.EnumAreaType[] areaTypes = ProgWidgetArea.EnumAreaType.values();
        for(int i = 0; i < areaTypes.length; i++) {
            GuiRadioButton radioButton = new GuiRadioButton(i, guiLeft + 4 + i / 4 * 60, guiTop + 80 + i % 4 * 12, 0xFF000000, areaTypes[i].toString());
            radioButton.checked = areaTypes[i] == ((ProgWidgetArea)widget).type;
            addWidget(radioButton);
            radioButtons.add(radioButton);
            radioButton.otherChoices = radioButtons;
        }
        if(invSearchGui != null) {
            ProgWidgetArea area = (ProgWidgetArea)widget;
            ChunkPosition pos = invSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(invSearchGui.getSearchStack()) : null;
            if(pos != null) {
                if(pointSearched == 0) {
                    area.x1 = pos.chunkPosX;
                    area.y1 = pos.chunkPosY;
                    area.z1 = pos.chunkPosZ;
                } else {
                    area.x2 = pos.chunkPosX;
                    area.y2 = pos.chunkPosY;
                    area.z2 = pos.chunkPosZ;
                }
            } else {
                if(pointSearched == 0) {
                    area.x1 = area.y1 = area.z1 = 0;
                } else {
                    area.x2 = area.y2 = area.z2 = 0;
                }
            }
        }
        NetworkHandler.sendToServer(new PacketProgrammerUpdate(guiProgrammer.te));
    }

    @Override
    public void actionPerformed(IGuiWidget guiWidget){
        ((ProgWidgetArea)widget).type = ProgWidgetArea.EnumAreaType.values()[guiWidget.getID()];
        super.actionPerformed(guiWidget);
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 0 || button.id == 1) {
            invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().thePlayer);
            ProgWidgetArea area = (ProgWidgetArea)widget;
            ItemStack gps = new ItemStack(Itemss.GPSTool);
            if(button.id == 0) {
                ItemGPSTool.setGPSLocation(gps, area.x1, area.y1, area.z1);
            } else {
                ItemGPSTool.setGPSLocation(gps, area.x2, area.y2, area.z2);
            }
            invSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : null);
            FMLClientHandler.instance().showGuiScreen(invSearchGui);
            pointSearched = button.id;
        } else {
            guiProgrammer.te.previewArea(widget.getX(), widget.getY());
            //PacketDispatcher.sendPacketToServer(PacketHandlerPneumaticCraft.getProgrammerUpdateWidgetPacket(guiProgrammer.te));
            //PacketDispatcher.sendPacketToServer(PacketHandlerPneumaticCraft.showDroneArea(guiProgrammer.te.xCoord, guiProgrammer.te.yCoord, guiProgrammer.te.zCoord, widget.getX(), widget.getY()));
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRendererObj.drawString("Point 1", guiLeft + 30, guiTop + 10, 0xFF000000);
        fontRendererObj.drawString("Point 2", guiLeft + 119, guiTop + 10, 0xFF000000);
        fontRendererObj.drawString("Area Type:", guiLeft + 4, guiTop + 70, 0xFF000000);
    }
}
