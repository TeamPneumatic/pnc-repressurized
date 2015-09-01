package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.text.WordUtils;

import pneumaticCraft.client.gui.semiblock.GuiLogisticsLiquidFilter;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetFluidFilter;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.inventory.ContainerAmadronAddTrade;
import pneumaticCraft.common.item.ItemAmadronTablet;
import pneumaticCraft.common.item.ItemGPSTool;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketAmadronTradeAdd;
import pneumaticCraft.common.recipes.AmadronOfferCustom;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiAmadronAddTrade extends GuiPneumaticContainerBase{
    private GuiSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiInventorySearcher gpsSearchGui;
    private GuiLogisticsLiquidFilter fluidGui;
    private boolean isSettingInput;

    private WidgetFluidFilter inputFluid;
    private WidgetFluidFilter outputFluid;

    private WidgetTextFieldNumber inputNumber, outputNumber;
    private WidgetLabel inputNumberLabel, outputNumberLabel;
    private GuiButton addButton;

    private ChunkPosition inputPosition, outputPosition;

    public GuiAmadronAddTrade(){
        super(new ContainerAmadronAddTrade(), null, Textures.GUI_WIDGET_OPTIONS_STRING);
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void initGui(){
        super.initGui();

        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade)inventorySlots;

        addLabel(I18n.format("gui.amadron.addTrade.selling"), guiLeft + 4, guiTop + 5);
        addLabel(I18n.format("gui.amadron.addTrade.buying"), guiLeft + 93, guiTop + 5);

        buttonList.add(new GuiButton(0, guiLeft + 4, guiTop + 20, 85, 20, "Search item..."));
        buttonList.add(new GuiButton(1, guiLeft + 4, guiTop + 42, 85, 20, "Search inv..."));
        buttonList.add(new GuiButton(2, guiLeft + 4, guiTop + 64, 85, 20, "Search fluid..."));
        buttonList.add(new GuiButton(3, guiLeft + 93, guiTop + 20, 85, 20, "Search item..."));
        buttonList.add(new GuiButton(4, guiLeft + 93, guiTop + 42, 85, 20, "Search inv..."));
        buttonList.add(new GuiButton(5, guiLeft + 93, guiTop + 64, 85, 20, "Search fluid..."));
        buttonList.add(addButton = new GuiButton(8, guiLeft + 50, guiTop + 164, 85, 20, "Add Trade"));

        Fluid oldInputFluid = inputFluid != null ? inputFluid.getFluid() : null;
        Fluid oldOutputFluid = outputFluid != null ? outputFluid.getFluid() : null;
        inputFluid = new WidgetFluidFilter(-1, guiLeft + 10, guiTop + 90);
        outputFluid = new WidgetFluidFilter(-1, guiLeft + 99, guiTop + 90);
        inputFluid.setFluid(oldInputFluid);
        outputFluid.setFluid(oldOutputFluid);
        addWidget(inputFluid);
        addWidget(outputFluid);

        GuiButtonSpecial gpsButton1 = new GuiButtonSpecial(6, guiLeft + 10, guiTop + 115, 20, 20, "");
        GuiButtonSpecial gpsButton2 = new GuiButtonSpecial(7, guiLeft + 99, guiTop + 115, 20, 20, "");
        gpsButton1.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.amadron.button.selectSellingBlock.tooltip"), 40).split(System.getProperty("line.separator"))));
        gpsButton2.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.amadron.button.selectPaymentBlock.tooltip"), 40).split(System.getProperty("line.separator"))));
        gpsButton1.setRenderStacks(new ItemStack(Itemss.GPSTool));
        gpsButton2.setRenderStacks(new ItemStack(Itemss.GPSTool));
        addWidget(gpsButton1);
        addWidget(gpsButton2);

        inputNumber = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 6, guiTop + 145, 40, fontRendererObj.FONT_HEIGHT).setValue(inputNumber != null ? inputNumber.getValue() : 0);
        outputNumber = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 95, guiTop + 145, 40, fontRendererObj.FONT_HEIGHT).setValue(outputNumber != null ? outputNumber.getValue() : 0);
        inputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
        outputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
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
        if(gpsSearchGui != null) {
            if(isSettingInput) {
                inputPosition = gpsSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(gpsSearchGui.getSearchStack()) : null;
            } else {
                outputPosition = gpsSearchGui.getSearchStack() != null ? ItemGPSTool.getGPSLocation(gpsSearchGui.getSearchStack()) : null;
            }
        }
        searchGui = null;
        fluidGui = null;
        invSearchGui = null;
        gpsSearchGui = null;

        inputNumberLabel = new WidgetLabel(guiLeft + 52, guiTop + 145, container.getStack(0) != null ? "x" : inputFluid.getFluid() != null ? "mB" : "");
        outputNumberLabel = new WidgetLabel(guiLeft + 149, guiTop + 145, container.getStack(1) != null ? "x" : outputFluid.getFluid() != null ? "mB" : "");
        addWidget(inputNumberLabel);
        addWidget(outputNumberLabel);
    }

    @Override
    public void actionPerformed(GuiButton button){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade)inventorySlots;
        if(button.id < 6 && button.id >= 0) {
            isSettingInput = button.id < 3;
            if(button.id % 3 == 0) {
                searchGui = new GuiSearcher(player);
                searchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(searchGui);
            } else if(button.id % 3 == 1) {
                invSearchGui = new GuiInventorySearcher(player);
                invSearchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(invSearchGui);
            } else if(button.id % 3 == 2) {
                fluidGui = new GuiLogisticsLiquidFilter(this);
                fluidGui.setFilter(isSettingInput ? inputFluid.getFluid() : outputFluid.getFluid());
                FMLClientHandler.instance().showGuiScreen(fluidGui);
            }

        } else if(button.id == 8) {
            Object input;
            if(container.getStack(0) != null) {
                input = container.getStack(0).copy();
                ((ItemStack)input).stackSize = inputNumber.getValue();
            } else {
                input = new FluidStack(inputFluid.getFluid(), inputNumber.getValue());
            }
            Object output;
            if(container.getStack(1) != null) {
                output = container.getStack(1).copy();
                ((ItemStack)output).stackSize = outputNumber.getValue();
            } else {
                output = new FluidStack(outputFluid.getFluid(), outputNumber.getValue());
            }
            AmadronOfferCustom trade = new AmadronOfferCustom(input, output, player);
            ChunkPosition pos = getInputPosition();
            int dimensionId = getInputDimension();
            trade.setProvidingPosition(pos, dimensionId);
            pos = getOutputPosition();
            dimensionId = getOutputDimension();
            trade.setReturningPosition(pos, dimensionId);
            NetworkHandler.sendToServer(new PacketAmadronTradeAdd(trade.invert()));
            player.closeScreen();
        }
        super.actionPerformed(button);
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        if(widget.getID() == 6 || widget.getID() == 7) {
            gpsSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClientPlayerEntity());
            isSettingInput = widget.getID() == 6;
            ItemStack gps = new ItemStack(Itemss.GPSTool);
            ChunkPosition pos;
            if(widget.getID() == 6) {
                pos = getInputPosition();
            } else {
                pos = getOutputPosition();
            }
            if(pos != null) ItemGPSTool.setGPSLocation(gps, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            gpsSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : null);
            FMLClientHandler.instance().showGuiScreen(gpsSearchGui);
        }
        super.actionPerformed(widget);
    }

    private ChunkPosition getInputPosition(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        return inputPosition != null ? inputPosition : ((ContainerAmadronAddTrade)inventorySlots).getStack(0) != null ? ItemAmadronTablet.getItemProvidingLocation(player.getCurrentEquippedItem()) : ItemAmadronTablet.getLiquidProvidingLocation(player.getCurrentEquippedItem());
    }

    private ChunkPosition getOutputPosition(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        return outputPosition != null ? outputPosition : ((ContainerAmadronAddTrade)inventorySlots).getStack(1) != null ? ItemAmadronTablet.getItemProvidingLocation(player.getCurrentEquippedItem()) : ItemAmadronTablet.getLiquidProvidingLocation(player.getCurrentEquippedItem());
    }

    private int getInputDimension(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        return inputPosition != null ? player.worldObj.provider.dimensionId : ((ContainerAmadronAddTrade)inventorySlots).getStack(0) != null ? ItemAmadronTablet.getItemProvidingDimension(player.getCurrentEquippedItem()) : ItemAmadronTablet.getLiquidProvidingDimension(player.getCurrentEquippedItem());
    }

    private int getOutputDimension(){
        EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
        return outputPosition != null ? player.worldObj.provider.dimensionId : ((ContainerAmadronAddTrade)inventorySlots).getStack(1) != null ? ItemAmadronTablet.getItemProvidingDimension(player.getCurrentEquippedItem()) : ItemAmadronTablet.getLiquidProvidingDimension(player.getCurrentEquippedItem());
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

    @Override
    public void updateScreen(){
        super.updateScreen();
        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade)inventorySlots;
        addButton.enabled = inputNumber.getValue() > 0 && outputNumber.getValue() > 0 && (inputFluid.getFluid() != null || container.getStack(0) != null) && (outputFluid.getFluid() != null || container.getStack(1) != null) && getInputPosition() != null && getOutputPosition() != null;
    }

    @Override
    protected void addProblems(List curInfo){
        if(getInputPosition() == null || getOutputPosition() == null) {
            curInfo.add("gui.amadron.addTrade.problems.noSellingOrPayingBlock");
        }
        super.addProblems(curInfo);
    }
}
