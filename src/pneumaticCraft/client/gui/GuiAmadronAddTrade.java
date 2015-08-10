package pneumaticCraft.client.gui;

import java.awt.Point;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.client.gui.semiblock.GuiLogisticsLiquidFilter;
import pneumaticCraft.client.gui.widget.WidgetFluidFilter;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.inventory.ContainerAmadronAddTrade;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiAmadronAddTrade extends GuiPneumaticContainerBase{
    private GuiSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiLogisticsLiquidFilter fluidGui;
    private boolean isSettingInput;

    private WidgetFluidFilter inputFluid;
    private WidgetFluidFilter outputFluid;

    private WidgetTextFieldNumber inputNumber, outputNumber;
    private WidgetLabel inputNumberLabel, outputNumberLabel;

    public GuiAmadronAddTrade(){
        super(new ContainerAmadronAddTrade(), null, Textures.GUI_WIDGET_OPTIONS_STRING);
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void initGui(){
        super.initGui();

        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade)inventorySlots;
        buttonList.add(new GuiButton(0, guiLeft + 4, guiTop + 20, 85, 20, "Search item..."));
        buttonList.add(new GuiButton(1, guiLeft + 4, guiTop + 42, 85, 20, "Search inventory..."));
        buttonList.add(new GuiButton(2, guiLeft + 4, guiTop + 64, 85, 20, "Search fluid..."));
        buttonList.add(new GuiButton(3, guiLeft + 93, guiTop + 20, 85, 20, "Search item..."));
        buttonList.add(new GuiButton(4, guiLeft + 93, guiTop + 42, 85, 20, "Search inventory..."));
        buttonList.add(new GuiButton(5, guiLeft + 93, guiTop + 64, 85, 20, "Search fluid..."));

        Fluid oldInputFluid = inputFluid != null ? inputFluid.getFluid() : null;
        Fluid oldOutputFluid = outputFluid != null ? outputFluid.getFluid() : null;
        inputFluid = new WidgetFluidFilter(-1, guiLeft + 10, guiTop + 90);
        outputFluid = new WidgetFluidFilter(-1, guiLeft + 86, guiTop + 90);
        inputFluid.setFluid(oldInputFluid);
        outputFluid.setFluid(oldOutputFluid);
        addWidget(inputFluid);
        addWidget(outputFluid);

        inputNumber = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 4, guiTop + 120, 40, fontRendererObj.FONT_HEIGHT).setValue(inputNumber != null ? inputNumber.getValue() : 0);
        outputNumber = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 85, guiTop + 120, 40, fontRendererObj.FONT_HEIGHT).setValue(outputNumber != null ? outputNumber.getValue() : 0);
        addWidget(inputNumber);
        addWidget(outputNumber);

        if(searchGui != null) {
            if(isSettingInput) {
                inputFluid.setFluid(null);
                container.setStack(0, searchGui.getSearchStack());
            } else {
                outputFluid.setFluid(null);
                container.setStack(1, searchGui.getSearchStack());
            }
        }
        if(invSearchGui != null) {
            if(isSettingInput) {
                inputFluid.setFluid(null);
                container.setStack(0, invSearchGui.getSearchStack());
            } else {
                outputFluid.setFluid(null);
                container.setStack(1, invSearchGui.getSearchStack());
            }
        }
        if(fluidGui != null) {
            if(isSettingInput) {
                container.setStack(0, null);
                inputFluid.setFluid(fluidGui.getFilter());
            } else {
                container.setStack(1, null);
                outputFluid.setFluid(fluidGui.getFilter());
            }
        }
        searchGui = null;
        fluidGui = null;
        invSearchGui = null;

        inputNumberLabel = new WidgetLabel(guiLeft + 50, guiTop + 120, container.getStack(0) != null ? "x" : inputFluid.getFluid() != null ? "mB" : "");
        outputNumberLabel = new WidgetLabel(guiLeft + 139, guiTop + 120, container.getStack(1) != null ? "x" : outputFluid.getFluid() != null ? "mB" : "");
        addWidget(inputNumberLabel);
        addWidget(outputNumberLabel);
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id < 6 && button.id >= 0) {
            ContainerAmadronAddTrade container = (ContainerAmadronAddTrade)inventorySlots;
            isSettingInput = button.id < 3;
            if(button.id % 3 == 0) {
                searchGui = new GuiSearcher(FMLClientHandler.instance().getClient().thePlayer);
                searchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(searchGui);
            } else if(button.id % 3 == 1) {
                invSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClient().thePlayer);
                invSearchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(invSearchGui);
            } else if(button.id % 3 == 2) {
                fluidGui = new GuiLogisticsLiquidFilter(this);
                fluidGui.setFilter(isSettingInput ? inputFluid.getFluid() : outputFluid.getFluid());
                FMLClientHandler.instance().showGuiScreen(fluidGui);
            }
        }
        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char key, int keyCode){
        /* if(keyCode == 1) {

         } else {*/
        super.keyTyped(key, keyCode);
        //}
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }
}
