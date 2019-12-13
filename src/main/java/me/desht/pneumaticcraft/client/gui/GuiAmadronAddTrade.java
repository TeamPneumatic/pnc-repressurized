package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsLiquidFilter;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetFluidFilter;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextFieldNumber;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeAddCustom;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeAddPeriodic;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeAddStatic;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeResource;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer.TradeType;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GuiAmadronAddTrade extends GuiPneumaticContainerBase<ContainerAmadronAddTrade, TileEntityBase> {
    private GuiItemSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiInventorySearcher gpsSearchGui;
    private GuiLogisticsLiquidFilter fluidGui;
    private boolean isSettingInput;

    private WidgetFluidFilter inputFluid;
    private WidgetFluidFilter outputFluid;

    private WidgetTextFieldNumber inputNumber, outputNumber;
    private Button addButton;

    private BlockPos inputPosition, outputPosition;
    private final TradeType tradeType;

    public GuiAmadronAddTrade(ContainerAmadronAddTrade container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        this.tradeType = getContainer().getTradeType();
        xSize = 183;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        addLabel(I18n.format("gui.amadron.addTrade.selling"), guiLeft + 4, guiTop + 5, 0xFFFFFFFF);
        addLabel(I18n.format("gui.amadron.addTrade.buying"), guiLeft + 93, guiTop + 5, 0xFFFFFFFF);

        addButton(new Button(guiLeft + 4, guiTop + 20, 85, 20,
                "Search item...", b -> openItemSearchGui(true)));
        addButton(new Button(guiLeft + 4, guiTop + 42, 85, 20,
                "Search inv...", b -> openInventorySearchGui(true)));
        addButton(new Button(guiLeft + 4, guiTop + 64, 85, 20,
                "Search fluid...", b -> openFluidSearchGui(true)));
        addButton(new Button(guiLeft + 93, guiTop + 20, 85, 20,
                "Search item...", b -> openItemSearchGui(false)));
        addButton(new Button(guiLeft + 93, guiTop + 42, 85, 20,
                "Search inv...", b -> openInventorySearchGui(false)));
        addButton(new Button(guiLeft + 93, guiTop + 64, 85, 20,
                "Search fluid...", b -> openFluidSearchGui(false)));

        addButton(addButton = new Button(guiLeft + 50, guiTop + 164, 85, 20, "Add Trade", b -> addTrade()));

        Fluid oldInputFluid = inputFluid != null ? inputFluid.getFluid() : null;
        Fluid oldOutputFluid = outputFluid != null ? outputFluid.getFluid() : null;
        inputFluid = new WidgetFluidFilter(guiLeft + 10, guiTop + 90);
        outputFluid = new WidgetFluidFilter(guiLeft + 99, guiTop + 90);
        inputFluid.setFluid(oldInputFluid);
        outputFluid.setFluid(oldOutputFluid);
        addButton(inputFluid);
        addButton(outputFluid);

        if (tradeType == TradeType.PLAYER) {
            WidgetButtonExtended gpsButton1 = new WidgetButtonExtended(guiLeft + 10, guiTop + 115, 20, 20, "", b -> openGPSGui(true));
            WidgetButtonExtended gpsButton2 = new WidgetButtonExtended(guiLeft + 99, guiTop + 115, 20, 20, "", b -> openGPSGui(false));
            gpsButton1.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.amadron.button.selectSellingBlock.tooltip"), 40).split(System.getProperty("line.separator"))));
            gpsButton2.setTooltipText(Arrays.asList(WordUtils.wrap(I18n.format("gui.amadron.button.selectPaymentBlock.tooltip"), 40).split(System.getProperty("line.separator"))));
            gpsButton1.setRenderStacks(new ItemStack(ModItems.GPS_TOOL));
            gpsButton2.setRenderStacks(new ItemStack(ModItems.GPS_TOOL));
            addButton(gpsButton1);
            addButton(gpsButton2);
        }

        inputNumber = new WidgetTextFieldNumber(font, guiLeft + 6, guiTop + 145, 40, font.FONT_HEIGHT).setValue(inputNumber != null ? inputNumber.getValue() : 0);
        outputNumber = new WidgetTextFieldNumber(font, guiLeft + 95, guiTop + 145, 40, font.FONT_HEIGHT).setValue(outputNumber != null ? outputNumber.getValue() : 0);
        inputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
        outputNumber.setTooltip(I18n.format("gui.amadron.addTrade.itemFluidAmount"));
        addButton(inputNumber);
        addButton(outputNumber);

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

        WidgetLabel inputNumberLabel = new WidgetLabel(guiLeft + 52, guiTop + 145, container.getInputStack().isEmpty() ? inputFluid.getFluid() != null ? "mB" : "" : "x", 0xFFFFFFFF);
        WidgetLabel outputNumberLabel = new WidgetLabel(guiLeft + 149, guiTop + 145, container.getOutputStack().isEmpty() ? outputFluid.getFluid() != null ? "mB" : "" : "x", 0xFFFFFFFF);
        addButton(inputNumberLabel);
        addButton(outputNumberLabel);
    }

    @Override
    protected int getBackgroundTint() {
        return 0x068e2c;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_WIDGET_OPTIONS;
    }

    private void openItemSearchGui(boolean isInput) {
        ClientUtils.openContainerGui(ModContainerTypes.SEARCHER, new StringTextComponent("Item Search"));
        if (minecraft.currentScreen instanceof GuiItemSearcher) {
            isSettingInput = isInput;
            searchGui = (GuiItemSearcher) minecraft.currentScreen;
            searchGui.setSearchStack(isSettingInput ? container.getInputStack() : container.getOutputStack());
        }
    }

    private void openInventorySearchGui(boolean isInput) {
        ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Inventory Search"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            isSettingInput = isInput;
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setSearchStack(isSettingInput ? container.getInputStack() : container.getOutputStack());
        }
    }

    private void openFluidSearchGui(boolean isInput) {
        isSettingInput = isInput;
        fluidGui = new GuiLogisticsLiquidFilter(this);
        fluidGui.setFilter(isSettingInput ? inputFluid.getFluid() : outputFluid.getFluid());
        minecraft.displayGuiScreen(fluidGui);
    }

    private void openGPSGui(boolean isInput) {
        ClientUtils.openContainerGui(ModContainerTypes.INVENTORY_SEARCHER, new StringTextComponent("Inventory Searcher (GPS)"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            gpsSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            gpsSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            isSettingInput = isInput;
            ItemStack gps = new ItemStack(ModItems.GPS_TOOL);
            GlobalPos gPos = getPosition(isInput ? ContainerAmadronAddTrade.INPUT_SLOT : ContainerAmadronAddTrade.OUTPUT_SLOT);
            if (gPos != null) ItemGPSTool.setGPSLocation(gps, gPos.getPos());
            gpsSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : ItemStack.EMPTY);
        }
    }

    private void addTrade() {
        TradeResource input;
        if (!container.getInputStack().isEmpty()) {
            input = TradeResource.of(ItemHandlerHelper.copyStackWithSize(container.getInputStack(), inputNumber.getValue()));
        } else {
            input = TradeResource.of(new FluidStack(inputFluid.getFluid(), inputNumber.getValue()));
        }
        TradeResource output;
        if (!container.getOutputStack().isEmpty()) {
            output = TradeResource.of(ItemHandlerHelper.copyStackWithSize(container.getOutputStack(), outputNumber.getValue()));
        } else {
            output = TradeResource.of(new FluidStack(outputFluid.getFluid(), outputNumber.getValue()));
        }
        if (tradeType == TradeType.PLAYER) {
            AmadronOfferCustom trade = new AmadronOfferCustom(input, output, minecraft.player);
            trade.setProvidingPosition(getPosition(ContainerAmadronAddTrade.INPUT_SLOT));
            trade.setReturningPosition(getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT));
            NetworkHandler.sendToServer(new PacketAmadronTradeAddCustom(trade.invert()));
        } else if (tradeType == TradeType.PERIODIC) {
            AmadronOffer trade = new AmadronOffer(input, output);
            NetworkHandler.sendToServer(new PacketAmadronTradeAddPeriodic(trade));
        } else if (tradeType == TradeType.STATIC) {
            AmadronOffer trade = new AmadronOffer(input, output);
            NetworkHandler.sendToServer(new PacketAmadronTradeAddStatic(trade));
        }
        minecraft.player.closeScreen();
    }

    private GlobalPos getPosition(int slot) {
        if (slot == ContainerAmadronAddTrade.INPUT_SLOT) {
            if (inputPosition != null) return GlobalPos.of(playerInventory.player.world.getDimension().getType(), inputPosition);
        } else if (slot == ContainerAmadronAddTrade.OUTPUT_SLOT) {
            if (outputPosition != null) return GlobalPos.of(playerInventory.player.world.getDimension().getType(), outputPosition);
        }
        if (container.getStack(slot).isEmpty()) {
            return ItemAmadronTablet.getFluidProvidingLocation(playerInventory.player.getHeldItemMainhand());
        } else {
            return ItemAmadronTablet.getItemProvidingLocation(playerInventory.player.getHeldItemMainhand());
        }
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        boolean posOK = tradeType != TradeType.PLAYER
                || (getPosition(ContainerAmadronAddTrade.INPUT_SLOT) != null && getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT) != null);
        addButton.active = inputNumber.getValue() > 0
                && outputNumber.getValue() > 0
                && (inputFluid.getFluid() != null || !container.getInputStack().isEmpty())
                && (outputFluid.getFluid() != null || !container.getOutputStack().isEmpty())
                && posOK;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        if (tradeType == TradeType.PLAYER && (getPosition(ContainerAmadronAddTrade.INPUT_SLOT) == null || getPosition(ContainerAmadronAddTrade.OUTPUT_SLOT) == null)) {
            curInfo.add("gui.amadron.addTrade.problems.noSellingOrPayingBlock");
        }
        super.addProblems(curInfo);
    }
}
