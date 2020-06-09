package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.semiblock.GuiLogisticsLiquidFilter;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade;
import me.desht.pneumaticcraft.common.item.ItemAmadronTablet;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketAmadronTradeAddCustom;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade.INPUT_SLOT;
import static me.desht.pneumaticcraft.common.inventory.ContainerAmadronAddTrade.OUTPUT_SLOT;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class GuiAmadronAddTrade extends GuiPneumaticContainerBase<ContainerAmadronAddTrade, TileEntityBase> {
    private GuiItemSearcher searchGui;
    private GuiInventorySearcher invSearchGui;
    private GuiInventorySearcher gpsSearchGui;
    private GuiLogisticsLiquidFilter fluidGui;
    private int settingSlot;

    private final WidgetFluidFilter[] fluidFilters = new WidgetFluidFilter[2];
    private final WidgetTextFieldNumber[] amountFields = new WidgetTextFieldNumber[2];
    private final BlockPos[] positions = new BlockPos[2];
    private Button addButton;

    public GuiAmadronAddTrade(ContainerAmadronAddTrade container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        xSize = 183;
        ySize = 202;
    }

    @Override
    public void init() {
        super.init();

        if (searchGui != null) {
            setStack(settingSlot, searchGui.getSearchStack());
        } else if (invSearchGui != null) {
            setStack(settingSlot, invSearchGui.getSearchStack());
        } else if (fluidGui != null) {
            setFluid(settingSlot, fluidGui.getFilter());
        } else if (gpsSearchGui != null) {
            positions[settingSlot] = gpsSearchGui.getSearchStack().isEmpty() ?
                    null : ItemGPSTool.getGPSLocation(gpsSearchGui.getSearchStack());
        }
        searchGui = null;
        fluidGui = null;
        invSearchGui = null;
        gpsSearchGui = null;

        initSide(INPUT_SLOT);
        initSide(OUTPUT_SLOT);
        setFocused(amountFields[INPUT_SLOT]);
        addButton(addButton = new Button(guiLeft + 50, guiTop + 171, 85, 20, "Add Trade", b -> addTrade()));

        addJeiFilterInfoTab();
    }

    public void setStack(int slot, ItemStack stack) {
        fluidFilters[slot].setFluid(Fluids.EMPTY);
        container.setStack(slot, stack);
    }

    public void setFluid(int slot, Fluid stack) {
        container.setStack(slot, ItemStack.EMPTY);
        fluidFilters[slot].setFluid(stack);
    }

    private void initSide(int slot) {
        int xOffset = slot == INPUT_SLOT ? 0 : 89;

        String s = slot == INPUT_SLOT ? "selling" : "buying";
        addButton(new WidgetLabel(guiLeft + 48 + xOffset, guiTop + 7,
                I18n.format("pneumaticcraft.gui.amadron.addTrade." + s), 0xFFFFFFFF).setAlignment(WidgetLabel.Alignment.CENTRE));

        addButton(new Button(guiLeft + 4 + xOffset, guiTop + 20, 85, 20,
                "Search item...", b -> openItemSearchGui(slot)));
        addButton(new Button(guiLeft + 4 + xOffset, guiTop + 42, 85, 20,
                "Search inv...", b -> openInventorySearchGui(slot)));
        addButton(new Button(guiLeft + 4 + xOffset, guiTop + 64, 85, 20,
                "Search fluid...", b -> openFluidSearchGui(slot)));

        Fluid prev = fluidFilters[slot] != null ? fluidFilters[slot].getFluid() : Fluids.EMPTY;
        addButton(fluidFilters[slot] = new WidgetFluidFilter(guiLeft + 37 + xOffset, guiTop + 90, prev));

        String tip = slot == INPUT_SLOT ? "pneumaticcraft.gui.amadron.button.selectSellingBlock.tooltip" : "pneumaticcraft.gui.amadron.button.selectPaymentBlock.tooltip";
        addButton(new WidgetButtonExtended(guiLeft + 10 + xOffset, guiTop + 115, 20, 20, "",
                b -> openGPSGui(slot))
                .setTooltipText(PneumaticCraftUtils.splitString(I18n.format(tip), 40))
                .setRenderStacks(new ItemStack(ModItems.GPS_TOOL.get()))
        );

        amountFields[slot] = new WidgetTextFieldNumber(font, guiLeft + 22 + xOffset, guiTop + 145, 40, font.FONT_HEIGHT)
                .setValue(amountFields[slot] != null ? amountFields[slot].getValue() : 1);
        addButton(amountFields[slot]);

        addButton(new WidgetLabel(guiLeft + 65 + xOffset, guiTop + 145,
                fluidFilters[slot].getFluid() != Fluids.EMPTY ? "mB" : "" , 0xFFFFFFFF));
        GlobalPos p = getPosition(slot);
        if (p != null && p.getDimension() == minecraft.world.getDimension().getType()) {
            BlockState state = minecraft.world.getBlockState(p.getPos());
            String name = new ItemStack(state.getBlock().asItem()).getDisplayName().getFormattedText();
            addButton(new WidgetLabel(guiLeft + 32 + xOffset, guiTop + 118,
                    name, 0xFFFFFFFF)).setScale(0.5f);
            addButton(new WidgetLabel(guiLeft + 32 + xOffset, guiTop + 124,
                    " @ " + posToString(p.getPos()), 0xFFFFFFFF)).setScale(0.5f);
        }
        if (positions[slot] == null) {
            addButton(new WidgetLabel(guiLeft + 32 + xOffset, guiTop + 130, "[Default]", 0xFFC0C0C0)).setScale(0.5f);
        }
    }

    private String posToString(BlockPos pos) {
        return pos == null ? "[Default]" : PneumaticCraftUtils.posToString(pos);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        NetworkHandler.sendToServer(new PacketGuiButton("showAmadron"));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            WidgetTextFieldNumber amountIn = amountFields[INPUT_SLOT];
            WidgetTextFieldNumber amountOut = amountFields[OUTPUT_SLOT];
            if (amountIn.isFocused()) {
                amountIn.setCursorPosition(amountIn.getCursorPosition());
                amountIn.setFocused2(false);
                setFocused(amountOut);
                amountOut.setCursorPosition(0);
                amountOut.setSelectionPos(amountOut.getText().length());
            } else if (amountOut.isFocused()) {
                amountOut.setCursorPosition(amountOut.getCursorPosition());
                amountOut.setFocused2(false);
                setFocused(amountIn);
                amountIn.setCursorPosition(0);
                amountIn.setSelectionPos(amountIn.getText().length());
            }
        }

        if (getFocused() instanceof WidgetTextField) {
            WidgetTextField tf = (WidgetTextField) getFocused();
            return tf.keyPressed(keyCode, scanCode, modifiers) || tf.canWrite() || super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected int getBackgroundTint() {
        return 0xFF068E2C;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AMADRON_ADD_TRADE;
    }

    private void openItemSearchGui(int slot) {
        ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(),
                new TranslationTextComponent("pneumaticcraft.gui.amadron.addTrade.itemSearch"));
        if (minecraft.currentScreen instanceof GuiItemSearcher) {
            settingSlot = slot;
            searchGui = (GuiItemSearcher) minecraft.currentScreen;
            searchGui.setSearchStack(container.getStack(slot));
        }
    }

    private void openInventorySearchGui(int slot) {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(),
                new TranslationTextComponent("pneumaticcraft.gui.amadron.addTrade.invSearch"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            settingSlot = slot;
            invSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            invSearchGui.setSearchStack(container.getStack(slot));
        }
    }

    private void openFluidSearchGui(int slot) {
        settingSlot = slot;
        fluidGui = new GuiLogisticsLiquidFilter(this);
        fluidGui.setFilter(fluidFilters[slot].getFluid());
        minecraft.displayGuiScreen(fluidGui);
    }

    private void openGPSGui(int slot) {
        ClientUtils.openContainerGui(ModContainers.INVENTORY_SEARCHER.get(),
                new TranslationTextComponent("pneumaticcraft.gui.amadron.addTrade.gpsSearch"));
        if (minecraft.currentScreen instanceof GuiInventorySearcher) {
            gpsSearchGui = (GuiInventorySearcher) minecraft.currentScreen;
            gpsSearchGui.setStackPredicate(itemStack -> itemStack.getItem() instanceof IPositionProvider);
            settingSlot = slot;
            ItemStack gps = new ItemStack(ModItems.GPS_TOOL.get());
            GlobalPos gPos = getPosition(slot);
            if (gPos != null) ItemGPSTool.setGPSLocation(gps, gPos.getPos());
            gpsSearchGui.setSearchStack(ItemGPSTool.getGPSLocation(gps) != null ? gps : ItemStack.EMPTY);
        }
    }

    private void addTrade() {
        AmadronTradeResource[] resources = new AmadronTradeResource[2];
        for (int slot = 0; slot < resources.length; slot++) {
            if (!container.getStack(slot).isEmpty()) {
                resources[slot] = AmadronTradeResource.of(ItemHandlerHelper.copyStackWithSize(container.getStack(slot), amountFields[slot].getValue()));
            } else {
                resources[slot] = AmadronTradeResource.of(new FluidStack(fluidFilters[slot].getFluid(), amountFields[slot].getValue()));
            }
        }
        String id = minecraft.player.getName().getUnformattedComponentText().toLowerCase() + "_" + (System.currentTimeMillis() / 1000);
        AmadronPlayerOffer trade = new AmadronPlayerOffer(RL(id), resources[OUTPUT_SLOT], resources[INPUT_SLOT], minecraft.player)
                .setProvidingPosition(getPosition(INPUT_SLOT))
                .setReturningPosition(getPosition(OUTPUT_SLOT));
        NetworkHandler.sendToServer(new PacketAmadronTradeAddCustom(trade));
        minecraft.player.closeScreen();
    }

    private GlobalPos getPosition(int slot) {
        if (positions[slot] != null) {
            return GlobalPos.of(playerInventory.player.world.getDimension().getType(), positions[slot]);
        }
        if (!container.getStack(slot).isEmpty()) {
            return ItemAmadronTablet.getItemProvidingLocation(playerInventory.player.getHeldItemMainhand());
        } else if (fluidFilters[slot].getFluid() != Fluids.EMPTY) {
            return ItemAmadronTablet.getFluidProvidingLocation(playerInventory.player.getHeldItemMainhand());
        } else {
            return null;
        }
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        GlobalPos inPos = getPosition(INPUT_SLOT);
        GlobalPos outPos = getPosition(OUTPUT_SLOT);

        addButton.active = amountFields[INPUT_SLOT].getValue() > 0 && amountFields[OUTPUT_SLOT].getValue() > 0
                && (fluidFilters[INPUT_SLOT].getFluid() != Fluids.EMPTY || !container.getInputStack().isEmpty())
                && (fluidFilters[OUTPUT_SLOT].getFluid() != Fluids.EMPTY || !container.getOutputStack().isEmpty())
                && inPos != null && outPos != null;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        if (getPosition(INPUT_SLOT) == null || getPosition(OUTPUT_SLOT) == null) {
            curInfo.add("pneumaticcraft.gui.amadron.addTrade.problems.noSellingOrPayingBlock");
        }
        super.addProblems(curInfo);
    }
}
