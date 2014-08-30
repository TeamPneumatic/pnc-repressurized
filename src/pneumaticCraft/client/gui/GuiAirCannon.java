package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerAirCannon;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityAirCannon;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAirCannon extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_AIR_CANNON_LOCATION);
    private final TileEntityAirCannon te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;
    private int gpsX;
    private int gpsY;
    private int gpsZ;

    public GuiAirCannon(InventoryPlayer player, TileEntityAirCannon teAirCannon){

        super(new ContainerAirCannon(player, teAirCannon));
        ySize = 176;
        te = teAirCannon;
        gpsX = teAirCannon.gpsX;
        gpsY = teAirCannon.gpsY;
        gpsZ = teAirCannon.gpsZ;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        statusStat = new GuiAnimatedStat(this, "Cannon Status", new ItemStack(Blockss.airCannon), xStart + xSize, 3, 0xFFFFAA00, problemStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        upgradeStat.scaleTextSize(0.7F);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_AIR_CANNON);
        upgradeStat.setText(GuiConstants.UPGRADES_AIR_CANNON);

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 171, yStart + 30, 170, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString("GPS", 50, 20, 4210752);
        fontRendererObj.drawString("Upgr.", 13, 19, 4210752);

        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 106 + 2, 4210752);

        redstoneButton.displayString = "Redstone signal" + (te.fireOnlyOnRightAngle ? " and right angle" : "");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(guiTexture);
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        drawTexturedModalRect(xStart, yStart, 0, 0, xSize, ySize);
        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_AIR_CANNON, PneumaticValues.DANGER_PRESSURE_AIR_CANNON, PneumaticValues.MIN_PRESSURE_AIR_CANNON, te.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4, yStart + ySize * 1 / 4 + 4, zLevel);
        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        statusStat.setText(getStatusText());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();

        if(gpsX != te.gpsX || gpsY != te.gpsY || gpsZ != te.gpsZ) {
            gpsX = te.gpsX;
            gpsY = te.gpsY;
            gpsZ = te.gpsZ;
            statusStat.openWindow();
        }
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.getPressure(ForgeDirection.UNKNOWN) * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(te.currentAir + te.volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_AIR_CANNON) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_AIR_CANNON;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }
        return pressureStatText;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();
        text.add("\u00a77Current Aimed Coordinate:");
        if(te.gpsX != 0 || te.gpsY != 0 || te.gpsZ != 0) {
            text.add("\u00a70X: " + te.gpsX + ", Y: " + te.gpsY + ", Z: " + te.gpsZ);
        } else {
            text.add("\u00a70- No coordinate selected -");
        }
        text.add("\u00a77Current Heading Angle:");
        text.add("\u00a70" + Math.round(te.rotationAngle) + " degrees.");
        text.add("\u00a77Current Height Angle:");
        text.add("\u00a70" + (90 - Math.round(te.heightAngle)) + " degrees.");
        text.add(EnumChatFormatting.GRAY + "Range");
        text.add(EnumChatFormatting.BLACK + "About " + PneumaticCraftUtils.roundNumberTo(te.getForce() * 25F, 0) + "m");
        return text;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        List<Pair<ForgeDirection, IPneumaticMachine>> teSurrounding = te.getConnectedPneumatics();
        if(te.getPressure(ForgeDirection.UNKNOWN) < PneumaticValues.MIN_PRESSURE_AIR_CANNON) {
            textList.add("\u00a77Not enough pressure");
            textList.add("\u00a70Add air to the input");
        }
        if(teSurrounding.isEmpty()) {
            textList.add("\u00a77No air input connected.");
            textList.add("\u00a70Add pipes / machines");
            textList.add("\u00a70to the input.");
        }
        if(te.getStackInSlot(0) == null) {
            textList.add("\u00a77No items to fire");
            textList.add("\u00a70Add items in the");
            textList.add("\u00a70cannon slot.");
        }
        if(!te.hasCoordinate()) {
            textList.add("\u00a77No destination coordinate set");
            textList.add("\u00a70Put a GPS Tool with a");
            textList.add("\u00a70coordinate set in the GPS slot.");
        } else if(!te.coordWithinReach) {
            textList.add("\u00a77Selected coordinate");
            textList.add("\u00a77can't be reached");
            textList.add("\u00a70Select a coordinate");
            textList.add("\u00a70closer to the cannon.");
        } else if(te.fireOnlyOnRightAngle && !te.doneTurning) {
            textList.add("\u00a77Cannon still turning");
            textList.add("\u00a77Wait for the cannon");
        }
        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            textList.add("\u00a70Apply a redstone");
            textList.add("\u00a70signal to fire.");
        }
        return textList;
    }

    public List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Fire upon                          ");
        for(int i = 0; i < 3; i++)
            textList.add("");// create some space for the button
        return textList;
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button){
        switch(button.id){
            case 0:// redstone button
                redstoneBehaviourStat.closeWindow();
                break;
        }
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

}
