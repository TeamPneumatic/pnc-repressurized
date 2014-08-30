package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerPressureChamberInterface;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.tileentity.TileEntityPressureChamberInterface;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPressureChamberInterface extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_PRESSURE_CHAMBER_INTERFACE_LOCATION);
    private static final ResourceLocation guiTextureCreativeFilter = new ResourceLocation(Textures.GUI_PRESSURE_CHAMBER_INTERFACE_CREATIVE_FILTER_LOCATION);
    private final TileEntityPressureChamberInterface te;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat filterStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;

    private GuiButton filterButton;
    private GuiButton creativeTabButton;
    private GuiButton redstoneButton;
    private GuiTextField nameFilterField;

    public GuiPressureChamberInterface(InventoryPlayer player, TileEntityPressureChamberInterface teInterface){

        super(new ContainerPressureChamberInterface(player, teInterface));
        ySize = 176;
        te = teInterface;
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, yStart + 5, 0xFFFF0000, null, false);
        statusStat = new GuiAnimatedStat(this, "Interface Status", new ItemStack(Blockss.pressureChamberInterface), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        filterStat = new GuiAnimatedStat(this, "Filter", new ItemStack(net.minecraft.init.Blocks.hopper), xStart + xSize, 3, 0xFF005500, statusStat, false);

        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);

        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);

        animatedStatList.add(redstoneBehaviourStat.setText(getRedstoneBehaviour()));
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        animatedStatList.add(filterStat);
        infoStat.setText(GuiConstants.INFO_PRESSURE_CHAMBER_INTERFACE);
        upgradeStat.setText(GuiConstants.UPGRADES_PRESSURE_CHAMBER_INTERFACE_TEXT);
        filterStat.setTextWithoutCuttingString(getFilterText());

        Rectangle filterRectangle = filterStat.getButtonScaledRectangle(xStart + 178, yStart + 68, 170, 20);
        filterButton = getButtonFromRectangle(0, filterRectangle, "-");
        creativeTabButton = new GuiButton(1, xStart + 91, yStart + 58, 78, 20, "-");
        nameFilterField = new GuiTextField(fontRendererObj, xStart + 91, yStart + 58, 78, 10);
        nameFilterField.setText(te.itemNameFilter);

        Rectangle redstoneButtonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 118, yStart + 30, 117, 20);
        redstoneButton = getButtonFromRectangle(2, redstoneButtonRect, "-");

        buttonList.add(redstoneButton);
        buttonList.add(filterButton);
        buttonList.add(creativeTabButton);
        if(te.filterMode != TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
            if(((Slot)inventorySlots.inventorySlots.get(5)).xDisplayPosition < 1000) {
                for(int i = 5; i < 14; i++) {
                    ((Slot)inventorySlots.inventorySlots.get(i)).xDisplayPosition += 1000;
                }
            }
        } else {
            if(((Slot)inventorySlots.inventorySlots.get(5)).xDisplayPosition > 1000) {
                for(int i = 5; i < 14; i++) {
                    ((Slot)inventorySlots.inventorySlots.get(i)).xDisplayPosition -= 1000;
                }
            }
        }
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Only transfer on       "); // the spaces are there
        // to create space for
        // the button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 4, 4210752);
        fontRendererObj.drawString("Item Filter", 115, 15, 4210752);
        fontRendererObj.drawString("Upgr.", 24, 16, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);
        creativeTabButton.visible = false;
        switch(te.filterMode){
            case ITEM:
                filterButton.displayString = "Items";
                nameFilterField.setFocused(false);
                nameFilterField.setVisible(false);
                break;
            case CREATIVE_TAB:
                filterButton.displayString = "Creative Tab";
                fontRendererObj.drawString("Tab Name:", 106, 45, 4210752);
                String tabName = I18n.format(CreativeTabs.creativeTabArray[te.creativeTabID].getTranslatedTabLabel());
                if(fontRendererObj.getStringWidth(tabName) > 75) {
                    while(fontRendererObj.getStringWidth(tabName) > 75) {
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
                fontRendererObj.drawString("Item Name", 106, 35, 4210752);
                fontRendererObj.drawString("begins with:", 103, 45, 4210752);
                nameFilterField.setVisible(true);
                break;
            case NAME_CONTAINS:
                filterButton.displayString = "Item Name (contains)";
                fontRendererObj.drawString("Item Name", 106, 35, 4210752);
                fontRendererObj.drawString("contains:", 108, 45, 4210752);
                nameFilterField.setVisible(true);

        }

        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Ignore Redstone";
                break;
            case 1:
                redstoneButton.displayString = "High Redstone Signal";
                break;
            case 2:
                redstoneButton.displayString = "Low Redstone Signal";
        }

        int inputShift = (int)((1F - (float)Math.cos((float)te.inputProgress / (float)TileEntityPressureChamberInterface.MAX_PROGRESS * Math.PI)) * 11);
        int outputShift = (int)((1F - (float)Math.cos((float)te.outputProgress / (float)TileEntityPressureChamberInterface.MAX_PROGRESS * Math.PI)) * 11);
        Gui.drawRect(63 + inputShift, 30, 87 + inputShift, 32, 0xFF5a62ff);
        Gui.drawRect(63 + outputShift, 54, 87 + outputShift, 56, 0xFFffa800);

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if(te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
            mc.getTextureManager().bindTexture(guiTexture);
        } else {
            mc.getTextureManager().bindTexture(guiTextureCreativeFilter);
        }
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        problemStat.setText(te.getProblemStat());
        statusStat.setText(getStatusText());

        nameFilterField.drawTextBox();
        filterButton.visible = filterStat.isDoneExpanding();
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();
        text.add("\u00a77Interface Mode:");
        switch(te.interfaceMode.ordinal()){
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

    public List<String> getFilterText(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Filter on                            "); // the
                                                                      // spaces
                                                                      // are there
                                                                      // to create
                                                                      // space for
                                                                      // the
                                                                      // button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);

        if(te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.NAME_BEGINS || te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.NAME_CONTAINS) {
            nameFilterField.mouseClicked(par1, par2, par3);
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */

    @Override
    protected void actionPerformed(GuiButton button){
        switch(button.id){
            case 0://redstone button 
                if(filterStat != null) {
                    filterStat.closeWindow();
                    if(te.filterMode == TileEntityPressureChamberInterface.EnumFilterMode.ITEM) {
                        for(int i = 5; i < 14; i++) {
                            ((Slot)inventorySlots.inventorySlots.get(i)).xDisplayPosition += 1000;
                        }
                    } else if(te.filterMode.ordinal() == TileEntityPressureChamberInterface.EnumFilterMode.values().length - 1) {
                        for(int i = 5; i < 14; i++) {
                            ((Slot)inventorySlots.inventorySlots.get(i)).xDisplayPosition -= 1000;
                        }
                    }
                }
                break;
            case 2:// redstone button
                redstoneBehaviourStat.closeWindow();
                break;
        }
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(nameFilterField.isFocused() && par2 != 1) {
            nameFilterField.textboxKeyTyped(par1, par2);
            te.itemNameFilter = nameFilterField.getText();
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }
}
