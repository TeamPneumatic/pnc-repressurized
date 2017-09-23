package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerPressureChamberInterface;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateTextfield;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiPressureChamberInterface extends GuiPneumaticContainerBase<TileEntityPressureChamberInterface> {
    private static final ResourceLocation GUI_TEXTURE_CREATIVE_FILTER = new ResourceLocation(Textures.GUI_PRESSURE_CHAMBER_INTERFACE_CREATIVE_FILTER_LOCATION);
    private static final int FILTER_SLOT_START = 41;

    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat filterStat;

    private GuiButtonSpecial filterButton;
    private GuiButton creativeTabButton;
    private GuiTextField nameFilterField;
    private boolean hasEnoughPressure = true;

    public GuiPressureChamberInterface(InventoryPlayer player, TileEntityPressureChamberInterface te) {

        super(new ContainerPressureChamberInterface(player, te), te, Textures.GUI_PRESSURE_CHAMBER_INTERFACE_LOCATION);
    }

    @Override
    public void initGui() {
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat("Interface Status", new ItemStack(Blockss.PRESSURE_CHAMBER_INTERFACE), 0xFFFFAA00, false);
        filterStat = addAnimatedStat("Filter", new ItemStack(Blocks.HOPPER), 0xFF005500, false);
        filterStat.setTextWithoutCuttingString(getFilterText());

        Rectangle buttonRect = filterStat.getButtonScaledRectangle(5, 30, 170, 20);
        filterButton = new GuiButtonSpecial(1, buttonRect.x, buttonRect.y, buttonRect.width, buttonRect.height, "-");
        filterStat.addWidget(filterButton);

        creativeTabButton = new GuiButton(2, xStart + 91, yStart + 58, 78, 20, "-");
        nameFilterField = new GuiTextField(-1, fontRenderer, xStart + 93, yStart + 58, 74, 10);
        nameFilterField.setText(te.itemNameFilter);

        buttonList.add(creativeTabButton);
        if (te.filterMode != TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
            if (inventorySlots.inventorySlots.get(FILTER_SLOT_START).xPos < 1000) {
                adjustFilterSlotXPos(1000);
            }
        } else {
            if (inventorySlots.inventorySlots.get(FILTER_SLOT_START).xPos > 1000) {
                adjustFilterSlotXPos(-1000);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {

        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Item Filter", 115, 15, 4210752);
        fontRenderer.drawString("Upgr.", 24, 16, 4210752);

        creativeTabButton.visible = false;
        switch (te.filterMode) {
            case ITEM:
                filterButton.displayString = "Items";
                nameFilterField.setFocused(false);
                nameFilterField.setVisible(false);
                break;
            case CREATIVE_TAB:
                filterButton.displayString = "Creative Tab";
                fontRenderer.drawString("Tab Name:", 106, 45, 4210752);
                String tabName = I18n.format(CreativeTabs.CREATIVE_TAB_ARRAY[te.creativeTabID].getTranslatedTabLabel());
                if (fontRenderer.getStringWidth(tabName) > 75) {
                    while (fontRenderer.getStringWidth(tabName) > 75) {
                        tabName = tabName.substring(0, tabName.length() - 2);
                    }
                    tabName = tabName + ".";
                }
                creativeTabButton.displayString = tabName;
                creativeTabButton.visible = true;
                nameFilterField.setFocused(false);
                nameFilterField.setVisible(false);
                break;
            case NAME_BEGINS:
                filterButton.displayString = "Item Name (begins with)";
                fontRenderer.drawString("Item Name", 106, 35, 4210752);
                fontRenderer.drawString("begins with:", 103, 45, 4210752);
                nameFilterField.setVisible(true);
                break;
            case NAME_CONTAINS:
                filterButton.displayString = "Item Name (contains)";
                fontRenderer.drawString("Item Name", 106, 35, 4210752);
                fontRenderer.drawString("contains:", 108, 45, 4210752);
                nameFilterField.setVisible(true);

        }

        int inputShift = (int) ((1F - (float) Math.cos((float) te.inputProgress / (float) TileEntityPressureChamberInterface.MAX_PROGRESS * Math.PI)) * 11);
        int outputShift = (int) ((1F - (float) Math.cos((float) te.outputProgress / (float) TileEntityPressureChamberInterface.MAX_PROGRESS * Math.PI)) * 11);
        Gui.drawRect(63 + inputShift, 30, 87 + inputShift, 32, 0xFF5a62ff);
        Gui.drawRect(63 + outputShift, 54, 87 + outputShift, 56, 0xFFffa800);

    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -2);
    }

    @Override
    protected void bindGuiTexture() {
        if (te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
            super.bindGuiTexture();
        } else {
            mc.getTextureManager().bindTexture(GUI_TEXTURE_CREATIVE_FILTER);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        nameFilterField.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
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
        switch (te.interfaceMode.ordinal()) {
            case 0:
                text.add("\u00a70None");
                break;
            case 1:
                text.add("\u00a70Import Mode");
                break;
            case 2:
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
            inventorySlots.inventorySlots.get(i).xPos += amount;
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);

        if (te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.NAME_BEGINS || te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.NAME_CONTAINS) {
            nameFilterField.mouseClicked(par1, par2, par3);
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */

    @Override
    public void actionPerformed(IGuiWidget widget) {
        if (widget.getID() == 1) {
            if (filterStat != null) {
                filterStat.closeWindow();
                if (te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
                    adjustFilterSlotXPos(1000);
                } else if (te.filterMode.ordinal() == TileEntityPressureChamberInterface.EnumFilterMode.values().length - 1) {
                    adjustFilterSlotXPos(-1000);
                }
            }
        }
        super.actionPerformed(widget);
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        curInfo.addAll(te.getProblemStat());
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (nameFilterField.isFocused() && par2 != 1) {
            nameFilterField.textboxKeyTyped(par1, par2);
            te.itemNameFilter = nameFilterField.getText();
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }
}
