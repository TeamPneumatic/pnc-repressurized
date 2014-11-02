package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevator extends GuiPneumaticContainerBase<TileEntityElevatorBase>{
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat floorNameStat;
    private int currentEditedFloor;
    private GuiTextField floorNameField;
    private GuiButton floorNameNext;
    private GuiButton floorNamePrevious;

    public GuiElevator(InventoryPlayer player, TileEntityElevatorBase te){
        super(new Container4UpgradeSlots(player, te), te, Textures.GUI_4UPGRADE_SLOTS);
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        statusStat = addAnimatedStat("Elevator Status", new ItemStack(Blockss.elevatorBase), 0xFFFFAA00, false);
        floorNameStat = addAnimatedStat("Floor Names", new ItemStack(Blockss.elevatorCaller), 0xFF005500, false);
        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());

        Rectangle fieldRectangle = floorNameStat.getButtonScaledRectangle(xStart + 182, yStart + 125, 150, 20);
        floorNameField = getTextFieldFromRectangle(fieldRectangle);
        floorNameField.setText(te.getFloorName(currentEditedFloor));

        Rectangle namePreviousRectangle = floorNameStat.getButtonScaledRectangle(xStart + 182, yStart + 100, 40, 20);
        floorNamePrevious = getButtonFromRectangle(1, namePreviousRectangle, "<-");
        buttonList.add(floorNamePrevious);

        Rectangle nameNextRectangle = floorNameStat.getButtonScaledRectangle(xStart + 292, yStart + 100, 40, 20);
        floorNameNext = getButtonFromRectangle(2, nameNextRectangle, "->");
        buttonList.add(floorNameNext);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected String getRedstoneButtonText(int mode){
        return mode == 0 ? "gui.tab.redstoneBehaviour.elevator.button.redstone" : "gui.tab.redstoneBehaviour.elevator.button.elevatorCallers";
    }

    @Override
    protected String getRedstoneString(){
        return "gui.tab.redstoneBehaviour.elevator.controlBy";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        floorNameField.drawTextBox();
    }

    @Override
    public void updateScreen(){
        super.updateScreen();

        statusStat.setText(getStatusText());
        floorNameField.setFocused(floorNameStat.isDoneExpanding());
        floorNameField.setVisible(floorNameStat.isDoneExpanding());
        floorNameNext.visible = floorNameStat.isDoneExpanding();
        floorNamePrevious.visible = floorNameStat.isDoneExpanding();
    }

    private List<String> getFloorNameStat(){
        List<String> textList = new ArrayList<String>();
        for(int i = 0; i < 3; i++)
            textList.add("");
        textList.add("\u00a77         Floor " + (currentEditedFloor + 1) + "                   ");
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Current Extension:");
        text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.extension, 1) + " meter");
        text.add("\u00a77Max Extension:");
        text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.getMaxElevatorHeight(), 1) + " meter");
        return text;
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        float elevatorHeight = te.getMaxElevatorHeight();
        if(elevatorHeight == te.extension) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The elevator can't extend anymore.", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Add (more) Elevator Frames on top of the elevator", GuiConstants.maxCharPerLineLeft));
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        int[] floorHeights = te.floorHeights;

        if(button.id == 1 || button.id == 2) {
            if(floorNameStat != null) floorNameStat.closeWindow();
            if(button.id == 1) {
                currentEditedFloor--;
                if(currentEditedFloor < 0) {
                    currentEditedFloor = floorHeights.length - 1;
                    if(floorHeights.length == 0) currentEditedFloor = 0;
                }
            } else {
                currentEditedFloor++;
                if(currentEditedFloor >= floorHeights.length) {
                    currentEditedFloor = 0;
                }
            }
            floorNameField.setText(te.getFloorName(currentEditedFloor));
            floorNameStat.setTextWithoutCuttingString(getFloorNameStat());
        } else {
            super.actionPerformed(button);
        }
    }

    /*@Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        if(floorNameStat.isDoneExpanding()) {
            floorNameField.mouseClicked(par1, par2, par3);
        }
    }*/

    @Override
    protected void keyTyped(char par1, int par2){
        if(floorNameField.isFocused() && par2 != 1) {
            floorNameField.textboxKeyTyped(par1, par2);
            te.setFloorName(currentEditedFloor, floorNameField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, currentEditedFloor));
        } else {
            super.keyTyped(par1, par2);
        }
    }
}
