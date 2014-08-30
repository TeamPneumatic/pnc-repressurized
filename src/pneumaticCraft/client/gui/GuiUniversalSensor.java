package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.universalSensor.ISensorSetting;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerUniversalSensor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.network.PacketUpdateTextfield;
import pneumaticCraft.common.sensor.SensorHandler;
import pneumaticCraft.common.tileentity.TileEntityUniversalSensor;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUniversalSensor extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_UNIVERSAL_SENSOR);
    private final TileEntityUniversalSensor te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiAnimatedStat sensorInfoStat;
    private GuiButton redstoneButton;
    private GuiTextField nameFilterField;
    private int page;
    private int maxPage;
    private static final int MAX_SENSORS_PER_PAGE = 4;
    private int ticksExisted;

    public GuiUniversalSensor(InventoryPlayer player, TileEntityUniversalSensor teUniversalSensor){
        super(new ContainerUniversalSensor(player, teUniversalSensor));
        ySize = 239;
        te = teUniversalSensor;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        sensorInfoStat = new GuiAnimatedStat(this, "Sensor Info", new ItemStack(Blockss.universalSensor), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(net.minecraft.init.Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);

        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(sensorInfoStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setText(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_UNIVERSAL_SENSOR);
        upgradeStat.setText(getUpgradeText());

        nameFilterField = new GuiTextField(fontRendererObj, xStart + 70, yStart + 58, 100, 10);
        nameFilterField.setText(te.getText(0));

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 118, yStart + 30, 117, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        updateButtons();//also adds the redstoneButton.
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);

        if(maxPage > 1) fontRendererObj.drawString(page + "/" + maxPage, 110, 46 + 22 * MAX_SENSORS_PER_PAGE, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 92, 4210752);
        fontRendererObj.drawString("Upgr.", 23, 98, 4210752);

        String[] folders = te.getSensorSetting().split("/");
        if(folders.length == 1) {
            ItemStack[] requiredStacks = SensorHandler.instance().getRequiredStacksFromText(folders[0]);
            for(int i = 0; i < requiredStacks.length; i++) {
                GuiUtils.drawItemStack(requiredStacks[i], 102 + i * 18, 20);
            }
        } else {
            fontRendererObj.drawString(folders[folders.length - 1], 102, 24, 4210752);
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

        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.DANGER_PRESSURE_UNIVERSAL_SENSOR, PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR, te.getPressure(ForgeDirection.UNKNOWN), xStart + 34, yStart + ySize * 1 / 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();
        redstoneButton.displayString = te.invertedRedstone ? "Inverted" : "Normal";
        nameFilterField.drawTextBox();

        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        if(sensor != null) {
            sensor.drawAdditionalInfo(fontRendererObj);
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);

        nameFilterField.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void keyTyped(char par1, int par2){
        if(nameFilterField.isFocused() && par2 != 1) {
            nameFilterField.textboxKeyTyped(par1, par2);
            te.setText(0, nameFilterField.getText());
            NetworkHandler.sendToServer(new PacketUpdateTextfield(te, 0));
        } else {
            super.keyTyped(par1, par2);
        }
    }

    public void updateButtons(){
        buttonList.clear();
        buttonList.add(redstoneButton);
        if(!te.getSensorSetting().equals("")) {
            buttonList.add(new GuiButton(1, guiLeft + 70, guiTop + 18, 30, 20, "back"));
        }
        String[] directories = SensorHandler.instance().getDirectoriesAtLocation(te.getSensorSetting());
        maxPage = (directories.length - 1) / MAX_SENSORS_PER_PAGE + 1;
        if(page > maxPage) page = maxPage;
        if(page < 1) page = 1;
        if(maxPage > 1) {
            buttonList.add(new GuiButton(2, guiLeft + 70, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "<--"));
            buttonList.add(new GuiButton(3, guiLeft + 138, guiTop + 40 + 22 * MAX_SENSORS_PER_PAGE, 30, 20, "-->"));
        }

        int buttonsOnPage = MAX_SENSORS_PER_PAGE;
        if(page == maxPage && (directories.length % MAX_SENSORS_PER_PAGE > 0 || directories.length == 0)) {
            buttonsOnPage = directories.length % MAX_SENSORS_PER_PAGE;
        }
        for(int i = 0; i < buttonsOnPage; i++) {
            String buttonText = directories[i + (page - 1) * MAX_SENSORS_PER_PAGE];
            if(SensorHandler.instance().getSensorFromPath(te.getSensorSetting() + "/" + buttonText) != null) {
                buttonText = EnumChatFormatting.YELLOW + buttonText;
            }
            int buttonID = i * 10 + 10 + (page - 1) * MAX_SENSORS_PER_PAGE * 10;
            int buttonX = guiLeft + 70;
            int buttonY = guiTop + 40 + i * 22;
            int buttonWidth = 98;
            int buttonHeight = 20;
            if(te.getSensorSetting().equals("")) {
                ItemStack[] requiredStacks = SensorHandler.instance().getRequiredStacksFromText(buttonText);
                GuiButtonSpecial button = new GuiButtonSpecial(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, "");
                button.setRenderStacks(requiredStacks);
                button.enabled = te.areGivenUpgradesInserted(requiredStacks);
                buttonList.add(button);
            } else {
                buttonList.add(new GuiButton(buttonID, buttonX, buttonY, buttonWidth, buttonHeight, buttonText));
            }
        }
        sensorInfoStat.setText(getSensorInfo());
        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        boolean textboxEnabled = sensor != null && sensor.needsTextBox();
        nameFilterField.setVisible(textboxEnabled);
        if(!textboxEnabled) nameFilterField.setFocused(false);

    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(te.getSensorSetting().equals("") && ticksExisted++ > 5) {
            ticksExisted = 0;
            updateButtons();
        }
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Redstone emission:     "); // the spaces are there
                                                        // to create space for
                                                        // the button
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    private List<String> getUpgradeText(){
        return SensorHandler.instance().getUpgradeInfo();
    }

    private List<String> getSensorInfo(){
        List<String> text = new ArrayList<String>();
        ISensorSetting sensor = SensorHandler.instance().getSensorFromPath(te.getSensorSetting());
        if(sensor != null) {
            String[] folders = te.getSensorSetting().split("/");
            text.add(EnumChatFormatting.GRAY + folders[folders.length - 1]);
            text.addAll(sensor.getDescription());
        } else {
            text.add(EnumChatFormatting.BLACK + "No sensor selected.");
        }
        return text;
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(te.getPressure(ForgeDirection.UNKNOWN), 1) + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_UNIVERSAL_SENSOR) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_UNIVERSAL_SENSOR;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }
        if(te.isSensorActive) {
            pressureStatText.add(EnumChatFormatting.GRAY + "Usage:");
            pressureStatText.add(EnumChatFormatting.BLACK + PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_UNIVERSAL_SENSOR, 1) + "mL/tick");
        }
        return pressureStatText;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(te.getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_UNIVERSAL_SENSOR) {
            textList.add(EnumChatFormatting.GRAY + "Not enough pressure!");
            textList.add(EnumChatFormatting.BLACK + "Apply more pressure.");
        }
        if(SensorHandler.instance().getSensorFromPath(te.getSensorSetting()) == null) {
            textList.add(EnumChatFormatting.GRAY + "No sensor selected!");
            textList.add(EnumChatFormatting.BLACK + "Insert upgrades and select the desired sensor.");
        }
        for(int i = TileEntityUniversalSensor.UPGRADE_SLOT_1; i <= TileEntityUniversalSensor.UPGRADE_SLOT_4; i++) {
            if(te.getStackInSlot(i) != null && te.getStackInSlot(i).getItem() == Itemss.GPSTool) {
                if(te.getStackInSlot(i).hasTagCompound()) {
                    NBTTagCompound gpsTag = te.getStackInSlot(i).getTagCompound();
                    int toolX = gpsTag.getInteger("x");
                    int toolY = gpsTag.getInteger("y");
                    int toolZ = gpsTag.getInteger("z");
                    if(toolX == 0 && toolY == 0 && toolZ == 0) {
                        textList.add(EnumChatFormatting.GRAY + "The inserted GPS Tool doesn't have a coordinate selected!");
                        textList.add(EnumChatFormatting.BLACK + "Insert a GPS Tool with stored coordinate.");
                        break;
                    }
                    int sensorRange = te.getRange();
                    if(Math.abs(toolX - te.xCoord) > sensorRange || Math.abs(toolY - te.yCoord) > sensorRange || Math.abs(toolZ - te.zCoord) > sensorRange) {
                        textList.add(EnumChatFormatting.GRAY + "The stored coordinate in the GPS Tool is out of the Sensor's range!");
                        textList.add(EnumChatFormatting.BLACK + "Move the sensor closer, select a closer coordinate or insert Range Upgrades.");
                    }
                } else {
                    textList.add(EnumChatFormatting.GRAY + "The inserted GPS Tool doesn't have a coordinate selected!");
                    textList.add(EnumChatFormatting.BLACK + "Insert a GPS Tool with stored coordinate.");
                }
                break;
            }
        }

        if(textList.size() == 0) {
            textList.add(EnumChatFormatting.BLACK + "No problems");
        }
        return textList;
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        if(button.id == 0) {
            redstoneBehaviourStat.closeWindow();
        }
        if(button.id == 2) {
            page--;
            if(page <= 0) page = maxPage;
            updateButtons();
        } else if(button.id == 3) {
            page++;
            if(page > maxPage) page = 1;
            updateButtons();
        } else {
            NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
        }
    }
}
