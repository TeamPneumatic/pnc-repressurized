package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
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
                inputPosition = gpsSearchGui.getSearchStack().isEmpty() ? null : ItemGPSTool.getGPSLocation(gpsSearchGui.getSearchStack());
            } else {
                outputPosition = gpsSearchGui.getSearchStack().isEmpty() ? null : ItemGPSTool.getGPSLocation(gpsSearchGui.getSearchStack());
            }
        }
        searchGui = null;
        fluidGui = null;
        invSearchGui = null;
        gpsSearchGui = null;

        WidgetLabel inputNumberLabel = new WidgetLabel(guiLeft + 52, guiTop + 145, container.getInputStack().isEmpty() ? inputFluid.getFluid() != null ? "mB" : "" : "x");
        WidgetLabel outputNumberLabel = new WidgetLabel(guiLeft + 149, guiTop + 145, container.getOutputStack().isEmpty() ? outputFluid.getFluid() != null ? "mB" : "" : "x");
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
                searchGui.setSearchStack(isSettingInput ? container.getInputStack() : container.getOutputStack());
                FMLClientHandler.instance().showGuiScreen(searchGui);
            } else if (button.id % 3 == 1) {
                invSearchGui = new GuiInventorySearcher(player);
                invSearchGui.setSearchStack(isSettingInput ? container.getInputStack() : container.getOutputStack());
                FMLClientHandler.instance().showGuiScreen(invSearchGui);
            } else if (button.id % 3 == 2) {
                fluidGui = new GuiLogisticsLiquidFilter(this);
                fluidGui.setFilter(isSettingInput ? inputFluid.getFluid() : outputFluid.getFluid());
                FMLClientHandler.instance().showGuiScreen(fluidGui);
            }

        } else if (button.id == 8) {
            Object input;
            if (!container.getInputStack().isEmpty()) {
                input = container.getInputStack().copy();
                ((ItemStack) input).setCount(inputNumber.getValue());
            } else {
                input = new FluidStack(inputFluid.getFluid(), inputNumber.getValue());
            }
            Object output;
            if (!container.getOutputStack().isEmpty()) {
                output = container.getOutputStack().copy();
                ((ItemStack) output).setCount(outputNumber.getValue());
            } else {
                output = new FluidStack(outputFluid.getFluid(), outputNumber.getValue());
            }
            AmadronOfferCustom trade = new AmadronOfferCustom(input, output, player);
            BlockPos pos = getPosition(ContainerAmadronAddTrade.INPUT_SLOT);
            int dimensionId = getDimension(ContainerAmadronAddTrade.INPUT_SLOT);
            trade.setProvidingPosition(pos, dimensionId);
            pos = getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT);
            dimensionId = getDimension(ContainerAmadronAddTrade.OUTPUT_SLOT);
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
            gpsSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            isSettingInput = widget.getID() == 6;
            ItemStack gps = new ItemStack(Itemss.GPS_TOOL);
            BlockPos pos;
            if (widget.getID() == 6) {
                pos = getPosition(ContainerAmadronAddTrade.INPUT_SLOT);
            } else {
                pos = getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT);
            }
            if (pos != null) ItemGPSTool.setGPSLocation(gps, pos);
            gpsSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : ItemStack.EMPTY);
            FMLClientHandler.instance().showGuiScreen(gpsSearchGui);
        }
        super.actionPerformed(widget);
    }

    private BlockPos getPosition(int slot) {
        if (slot == ContainerAmadronAddTrade.INPUT_SLOT) {
            if (inputPosition != null) return inputPosition;
        } else if (slot == ContainerAmadronAddTrade.OUTPUT_SLOT) {
            if (outputPosition != null) return outputPosition;
        }
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        if (((ContainerAmadronAddTrade) inventorySlots).getStack(slot).isEmpty()) {
            return ItemAmadronTablet.getLiquidProvidingLocation(player.getHeldItemMainhand());
        } else {
            return ItemAmadronTablet.getItemProvidingLocation(player.getHeldItemMainhand());
        }
    }

    private int getDimension(int slot) {
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        if (slot == ContainerAmadronAddTrade.INPUT_SLOT) {
            if (inputPosition != null) return player.world.provider.getDimension();
        } else if (slot == ContainerAmadronAddTrade.OUTPUT_SLOT) {
            if (outputPosition != null) return player.world.provider.getDimension();
        }
        if (((ContainerAmadronAddTrade) inventorySlots).getStack(slot).isEmpty()) {
            return ItemAmadronTablet.getLiquidProvidingDimension(player.getHeldItemMainhand());
        } else {
            return ItemAmadronTablet.getItemProvidingDimension(player.getHeldItemMainhand());
        }
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ContainerAmadronAddTrade container = (ContainerAmadronAddTrade) inventorySlots;
        addButton.enabled = inputNumber.getValue() > 0
                && outputNumber.getValue() > 0
                && (inputFluid.getFluid() != null || !container.getInputStack().isEmpty())
                && (outputFluid.getFluid() != null || !container.getOutputStack().isEmpty())
                && getPosition(ContainerAmadronAddTrade.INPUT_SLOT) != null
                && getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT) != null;
    }

    @Override
    protected void addProblems(List curInfo) {
        if (getPosition(ContainerAmadronAddTrade.INPUT_SLOT) == null || getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT) == null) {
            curInfo.add("gui.amadron.addTrade.problems.noSellingOrPayingBlock");
        }
        super.addProblems(curInfo);
    }
}
