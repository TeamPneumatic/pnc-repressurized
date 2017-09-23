package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsLiquidFilter;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetFluidFilter;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeAdd;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GuiAmadronAddTrade extends GuiPneumaticContainerBase {
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

    private BlockPos inputPosition, outputPosition;

    public GuiAmadronAddTrade() {
        super(new ContainerAmadronAddTrade(), null, Textures.GUI_WIDGET_OPTIONS_STRING);
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void initGui() {
        super.initGui();

        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade) inventorySlots;

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
        gpsButton1.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        gpsButton2.setRenderStacks(new ItemStack(Itemss.GPS_TOOL));
        addWidget(gpsButton1);
        addWidget(gpsButton2);

        inputNumber = new WidgetTextFieldNumber(fontRenderer, guiLeft + 6, guiTop + 145, 40, fontRenderer.FONT_HEIGHT).setValue(inputNumber != null ? inputNumber.getValue() : 0);
        outputNumber = new WidgetTextFieldNumber(fontRenderer, guiLeft + 95, guiTop + 145, 40, fontRenderer.FONT_HEIGHT).setValue(outputNumber != null ? outputNumber.getValue() : 0);
        inputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
        outputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
        addWidget(inputNumber);
        addWidget(outputNumber);

        if (searchGui != null) {
            if (isSettingInput) {
                inputFluid.setFluid(null);
                container.setStack(0, searchGui.getSearchStack());
            } else {
                outputFluid.setFluid(null);
                container.setStack(1, searchGui.getSearchStack());
            }
        }
        if (invSearchGui != null) {
            if (isSettingInput) {
                inputFluid.setFluid(null);
                container.setStack(0, invSearchGui.getSearchStack());
            } else {
                outputFluid.setFluid(null);
                container.setStack(1, invSearchGui.getSearchStack());
            }
        }
        if (fluidGui != null) {
            if (isSettingInput) {
                container.setStack(0, ItemStack.EMPTY);
                inputFluid.setFluid(fluidGui.getFilter());
            } else {
                container.setStack(1, ItemStack.EMPTY);
                outputFluid.setFluid(fluidGui.getFilter());
            }
        }
        if (gpsSearchGui != null) {
            if (isSettingInput) {
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
    public void actionPerformed(GuiButton button) {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade) inventorySlots;
        if (button.id < 6 && button.id >= 0) {
            isSettingInput = button.id < 3;
            if (button.id % 3 == 0) {
                searchGui = new GuiSearcher(player);
                searchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(searchGui);
            } else if (button.id % 3 == 1) {
                invSearchGui = new GuiInventorySearcher(player);
                invSearchGui.setSearchStack(container.getStack(isSettingInput ? 0 : 1));
                FMLClientHandler.instance().showGuiScreen(invSearchGui);
            } else if (button.id % 3 == 2) {
                fluidGui = new GuiLogisticsLiquidFilter(this);
                fluidGui.setFilter(isSettingInput ? inputFluid.getFluid() : outputFluid.getFluid());
                FMLClientHandler.instance().showGuiScreen(fluidGui);
            }

        } else if (button.id == 8) {
            Object input;
            if (!container.getStack(0).isEmpty()) {
                input = container.getStack(0).copy();
                ((ItemStack) input).setCount(inputNumber.getValue());
            } else {
                input = new FluidStack(inputFluid.getFluid(), inputNumber.getValue());
            }
            Object output;
            if (!container.getStack(1).isEmpty()) {
                output = container.getStack(1).copy();
                ((ItemStack) output).setCount(outputNumber.getValue());
            } else {
                output = new FluidStack(outputFluid.getFluid(), outputNumber.getValue());
            }
            AmadronOfferCustom trade = new AmadronOfferCustom(input, output, player);
            BlockPos pos = getInputPosition();
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
    public void actionPerformed(IGuiWidget widget) {
        if (widget.getID() == 6 || widget.getID() == 7) {
            gpsSearchGui = new GuiInventorySearcher(FMLClientHandler.instance().getClientPlayerEntity());
            isSettingInput = widget.getID() == 6;
            ItemStack gps = new ItemStack(Itemss.GPS_TOOL);
            BlockPos pos;
            if (widget.getID() == 6) {
                pos = getInputPosition();
            } else {
                pos = getOutputPosition();
            }
            if (pos != null) ItemGPSTool.setGPSLocation(gps, pos);
            gpsSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : null);
            FMLClientHandler.instance().showGuiScreen(gpsSearchGui);
        }
        super.actionPerformed(widget);
    }

    private BlockPos getInputPosition() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        return inputPosition != null ? inputPosition : ((ContainerAmadronAddTrade) inventorySlots).getStack(0) != null ? ItemAmadronTablet.getItemProvidingLocation(player.getHeldItemMainhand()) : ItemAmadronTablet.getLiquidProvidingLocation(player.getHeldItemMainhand());
    }

    private BlockPos getOutputPosition() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        return outputPosition != null ? outputPosition : ((ContainerAmadronAddTrade) inventorySlots).getStack(1) != null ? ItemAmadronTablet.getItemProvidingLocation(player.getHeldItemMainhand()) : ItemAmadronTablet.getLiquidProvidingLocation(player.getHeldItemMainhand());
    }

    private int getInputDimension() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        return inputPosition != null ? player.world.provider.getDimension() : ((ContainerAmadronAddTrade) inventorySlots).getStack(0) != null ? ItemAmadronTablet.getItemProvidingDimension(player.getHeldItemMainhand()) : ItemAmadronTablet.getLiquidProvidingDimension(player.getHeldItemMainhand());
    }

    private int getOutputDimension() {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        return outputPosition != null ? player.world.provider.getDimension() : ((ContainerAmadronAddTrade) inventorySlots).getStack(1) != null ? ItemAmadronTablet.getItemProvidingDimension(player.getHeldItemMainhand()) : ItemAmadronTablet.getLiquidProvidingDimension(player.getHeldItemMainhand());
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade) inventorySlots;
        addButton.enabled = inputNumber.getValue() > 0 && outputNumber.getValue() > 0 && (inputFluid.getFluid() != null || container.getStack(0) != null) && (outputFluid.getFluid() != null || container.getStack(1) != null) && getInputPosition() != null && getOutputPosition() != null;
    }

    @Override
    protected void addProblems(List curInfo) {
        if (getInputPosition() == null || getOutputPosition() == null) {
            curInfo.add("gui.amadron.addTrade.problems.noSellingOrPayingBlock");
        }
        super.addProblems(curInfo);
    }
}
