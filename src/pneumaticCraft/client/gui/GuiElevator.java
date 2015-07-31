package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetTextField;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerElevator;
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
    private WidgetTextField floorNameField;

    public GuiElevator(InventoryPlayer player, TileEntityElevatorBase te){
        super(new ContainerElevator(player, te), te, Textures.GUI_ELEVATOR);
    }

    @Override
    public void initGui(){
        super.initGui();
        statusStat = addAnimatedStat("Elevator Status", new ItemStack(Blockss.elevatorBase), 0xFFFFAA00, false);
        floorNameStat = addAnimatedStat("Floor Names", new ItemStack(Blockss.elevatorCaller), 0xFF005500, false);
        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());

        Rectangle fieldRectangle = floorNameStat.getButtonScaledRectangle(6, 60, 160, 20);
        floorNameField = getTextFieldFromRectangle(fieldRectangle);
        floorNameField.setText(te.getFloorName(currentEditedFloor));
        floorNameStat.addWidget(floorNameField);

        Rectangle namePreviousRectangle = floorNameStat.getButtonScaledRectangle(5, 35, 40, 20);
        floorNameStat.addWidget(getButtonFromRectangle(1, namePreviousRectangle, "<-"));

        Rectangle nameNextRectangle = floorNameStat.getButtonScaledRectangle(125, 35, 40, 20);
        floorNameStat.addWidget(getButtonFromRectangle(2, nameNextRectangle, "->"));

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
        fontRendererObj.drawString("Camo", 73, 26, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode){
        return mode == 0 ? "gui.tab.redstoneBehaviour.elevator.button.redstone" : "gui.tab.redstoneBehaviour.elevator.button.elevatorCallers";
    }

    @Override
    public String getRedstoneString(){
        return "gui.tab.redstoneBehaviour.elevator.controlBy";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        statusStat.setText(getStatusText());
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

    @Override
    public void actionPerformed(IGuiWidget widget){
        super.actionPerformed(widget);

        if(widget.getID() == 1 || widget.getID() == 2) {
            int[] floorHeights = te.floorHeights;

            if(widget.getID() == 1) {
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
        }
    }

    @Override
    public void onKeyTyped(IGuiWidget widget){
        te.setFloorName(currentEditedFloor, floorNameField.getText());
        NetworkHandler.sendToServer(new PacketUpdateTextfield(te, currentEditedFloor));
    }
}
