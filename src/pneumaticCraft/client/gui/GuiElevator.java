package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.Container4UpgradeSlots;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.tileentity.TileEntityElevatorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiElevator extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_4UPGRADE_SLOTS);
    private final TileEntityElevatorBase te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat floorNameStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;
    private int currentEditedFloor;
    private GuiTextField floorNameField;
    private GuiButton floorNameNext;
    private GuiButton floorNamePrevious;

    public GuiElevator(InventoryPlayer player, TileEntityElevatorBase teElevator){
        super(new Container4UpgradeSlots(player, teElevator));
        ySize = 176;
        te = teElevator;
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        statusStat = new GuiAnimatedStat(this, "Elevator Status", new ItemStack(Blockss.elevatorBase), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        floorNameStat = new GuiAnimatedStat(this, "Floor Names", new ItemStack(Blockss.elevatorCaller), xStart + xSize, 3, 0xFF005500, statusStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(floorNameStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());
        floorNameStat.setTextWithoutCuttingString(getFloorNameStat());

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 131, yStart + 30, 130, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);

        Rectangle fieldRectangle = floorNameStat.getButtonScaledRectangle(xStart + 182, yStart + 125, 150, 20);
        floorNameField = getTextFieldFromRectangle(fieldRectangle);
        floorNameField.setText(te.getFloorName(currentEditedFloor));

        Rectangle namePreviousRectangle = floorNameStat.getButtonScaledRectangle(xStart + 182, yStart + 100, 40, 20);
        floorNamePrevious = getButtonFromRectangle(1, namePreviousRectangle, "<-");
        buttonList.add(floorNamePrevious);

        Rectangle nameNextRectangle = floorNameStat.getButtonScaledRectangle(xStart + 292, yStart + 100, 40, 20);
        floorNameNext = getButtonFromRectangle(2, nameNextRectangle, "->");
        buttonList.add(floorNameNext);

        infoStat.setText(GuiConstants.INFO_ELEVATOR);
        upgradeStat.setText(GuiConstants.UPGRADES_ELEVATOR);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);

        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Redstone";
                break;
            case 1:
                redstoneButton.displayString = "Elevator Callers";
                break;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_ELEVATOR, PneumaticValues.DANGER_PRESSURE_ELEVATOR, PneumaticValues.MIN_PRESSURE_ELEVATOR, te.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);

        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        statusStat.setText(getStatusText());

        floorNameField.setFocused(floorNameStat.isDoneExpanding());
        floorNameField.setVisible(floorNameStat.isDoneExpanding());
        floorNameNext.visible = floorNameStat.isDoneExpanding();
        floorNamePrevious.visible = floorNameStat.isDoneExpanding();
        floorNameField.drawTextBox();
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Control the Elevator by  "); // the spaces are
                                                          // there to create
                                                          // space for the
                                                          // button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getFloorNameStat(){
        List<String> textList = new ArrayList<String>();
        for(int i = 0; i < 3; i++)
            textList.add("");
        textList.add("\u00a77         Floor " + (currentEditedFloor + 1) + "                   "); // the spaces are
        // there to create
        // space for the
        // button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(ForgeDirection.UNKNOWN) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_ELEVATOR) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_ELEVATOR;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }
        return pressureStatText;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Current Extension:");
        text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.extension, 1) + " meter");
        text.add("\u00a77Max Extension:");
        text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.getMaxElevatorHeight(), 1) + " meter");
        return text;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        float elevatorHeight = te.getMaxElevatorHeight();
        if(elevatorHeight == te.extension) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The elevator can't extend anymore.", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Add (more) Elevator Frames on top of the elevator", GuiConstants.maxCharPerLineLeft));
        }
        if(te.getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_ELEVATOR) {
            textList.add("\u00a77The pressure is too low.");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Apply more pressure to the input.", GuiConstants.maxCharPerLineLeft));
        }
        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
        }
        return textList;
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        int[] floorHeights = te.floorHeights;
        switch(button.id){
            case 0:// redstone button
                if(redstoneBehaviourStat != null) redstoneBehaviourStat.closeWindow();
                break;
        }

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
            NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
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
