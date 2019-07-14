package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberInterface;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiPressureChamberInterface extends GuiPneumaticContainerBase<ContainerPressureChamberInterface,TileEntityPressureChamberInterface> {
    private static final ResourceLocation GUI_TEXTURE_CREATIVE_FILTER = Textures.GUI_PRESSURE_CHAMBER_INTERFACE_CREATIVE_FILTER_LOCATION;
    private static final int FILTER_SLOT_START = 41;
    private static final int HIDE_SLOTS_OFFSET = 10000;

    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat filterStat;

    private GuiButtonSpecial filterButton;
    private TextFieldWidget nameFilterField;
    private boolean hasEnoughPressure = true;
    private int sendDelay = -1;

    public GuiPressureChamberInterface(ContainerPressureChamberInterface container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat("Interface Status", new ItemStack(ModBlocks.PRESSURE_CHAMBER_INTERFACE), 0xFFFFAA00, false);
        filterStat = addAnimatedStat("Filter", new ItemStack(Blocks.HOPPER), 0xFF005500, false);
        filterStat.setTextWithoutCuttingString(getFilterText());

        filterButton = new GuiButtonSpecial(5, 30, 165, 20, "-", b -> cycleFilterMode()).withTag("filter_mode");
        filterStat.addSubWidget(filterButton);

        nameFilterField = new TextFieldWidget(font, xStart + 93, yStart + 58, 74, 10, "");
        nameFilterField.setText(te.itemNameFilter);
        nameFilterField.func_212954_a(s -> sendFilterTextDelayed());

        if (te.filterMode != TileEntityPressureChamberInterface.FilterMode.ITEM) {
            if (container.inventorySlots.get(FILTER_SLOT_START).xPos < HIDE_SLOTS_OFFSET) {
                adjustFilterSlotXPos(HIDE_SLOTS_OFFSET);
            }
        } else {
            if (container.inventorySlots.get(FILTER_SLOT_START).xPos > HIDE_SLOTS_OFFSET) {
                adjustFilterSlotXPos(-HIDE_SLOTS_OFFSET);
            }
        }
    }

    private void sendFilterTextDelayed() {
        sendDelay = 5;
    }

    private void cycleFilterMode() {
        if (filterStat != null) {
            filterStat.closeWindow();
            if (te.filterMode == TileEntityPressureChamberInterface.FilterMode.ITEM) {
                adjustFilterSlotXPos(HIDE_SLOTS_OFFSET);
            } else if (te.filterMode.ordinal() == TileEntityPressureChamberInterface.FilterMode.values().length - 1) {
                adjustFilterSlotXPos(-HIDE_SLOTS_OFFSET);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        font.drawString("Item Filter", 115, 15, 4210752);
        font.drawString("Upgr.", 24, 16, 4210752);

        switch (te.filterMode) {
            case NAME_BEGINS:
                font.drawString("Item Name", 106, 35, 0x404040);
                font.drawString("begins with:", 103, 45, 0x404040);
                break;
            case NAME_CONTAINS:
                font.drawString("Item Name", 106, 35, 0x404040);
                font.drawString("contains:", 108, 45, 0x404040);
        }

        float progress = TileEntityPressureChamberInterface.MAX_PROGRESS * (float)Math.PI;
        int inputShift = (int) ((1F - MathHelper.cos(te.inputProgress / progress)) * 11);
        int outputShift = (int) ((1F - MathHelper.cos(te.outputProgress / progress)) * 11);
        fill(63 + inputShift, 30, 87 + inputShift, 32, 0xFF5A62FF);
        fill(63 + outputShift, 54, 87 + outputShift, 56, 0xFFFFA800);

    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -2);
    }

    @Override
    protected void bindGuiTexture() {
        if (te.filterMode == TileEntityPressureChamberInterface.FilterMode.ITEM) {
            super.bindGuiTexture();
        } else {
            minecraft.getTextureManager().bindTexture(GUI_TEXTURE_CREATIVE_FILTER);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PRESSURE_CHAMBER_INTERFACE_LOCATION;
    }

    @Override
    public void tick() {
        super.tick();

        if (sendDelay > 0 && --sendDelay == 0) {
            te.itemNameFilter = nameFilterField.getText();
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
            sendDelay = -1;
        }

        switch (te.filterMode) {
            case ITEM:
                filterButton.setMessage("Items");
                nameFilterField.setFocused2(false);
                nameFilterField.setVisible(false);
                break;
            case NAME_BEGINS:
                filterButton.setMessage("Item Name (begins with)");
                nameFilterField.setVisible(true);
                break;
            case NAME_CONTAINS:
                filterButton.setMessage("Item Name (contains)");
                nameFilterField.setVisible(true);
        }

        statusStat.setText(getStatusText());
        filterButton.visible = filterStat.isDoneExpanding();
        if (hasEnoughPressure && !te.hasEnoughPressure()) {
            hasEnoughPressure = false;
            problemTab.openWindow();
        } else if (te.hasEnoughPressure()) {
            hasEnoughPressure = true;
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();
        text.add("\u00a77Interface Mode:");
        switch (te.interfaceMode) {
            case NONE:
                text.add("\u00a70None");
                break;
            case IMPORT:
                text.add("\u00a70Import Mode");
                break;
            case EXPORT:
                text.add("\u00a70Export Mode");
                break;
        }
        return text;
    }

    private List<String> getFilterText() {
        List<String> textList = new ArrayList<>();
        textList.add("\u00a77Filter on                            "); // the spaces are there to create space for the button
        for (int i = 0; i < 3; i++)
            textList.add(""); // create some space for the button
        return textList;
    }

    private void adjustFilterSlotXPos(int amount) {
        // we only want to display the filter slots when the filter type is ITEM
        for (int i = FILTER_SLOT_START; i < 50; i++) {
            container.inventorySlots.get(i).xPos += amount;
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        curInfo.addAll(te.getProblemStat());
    }
}
