package pneumaticCraft.client.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.PneumaticCraftUtils;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerChargingStation;
import pneumaticCraft.common.item.IChargingStationGUIHolderItem;
import pneumaticCraft.common.item.ItemPressurizable;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChargingStation extends GuiPneumaticContainerBase{
    private static final ResourceLocation guiTexture = new ResourceLocation(Textures.GUI_CHARGING_STATION_LOCATION);
    private final TileEntityChargingStation te;
    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat problemStat;
    private GuiAnimatedStat redstoneBehaviourStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat upgradeStat;
    private GuiButton redstoneButton;
    private GuiButton guiSelectButton;

    public GuiChargingStation(InventoryPlayer player, TileEntityChargingStation teChargingStation){

        super(new ContainerChargingStation(player, teChargingStation));
        ySize = 176;
        te = teChargingStation;

    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new GuiButton(1, xStart + 90, yStart + 15, 25, 20, "inv.");
        buttonList.add(guiSelectButton);
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        problemStat = new GuiAnimatedStat(this, "Problems", Textures.GUI_PROBLEMS_TEXTURE, xStart + xSize, 3, 0xFFFF0000, pressureStat, false);
        redstoneBehaviourStat = new GuiAnimatedStat(this, "Redstone Behaviour", new ItemStack(Items.redstone), xStart, yStart + 5, 0xFFCC0000, null, true);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, 3, 0xFF8888FF, redstoneBehaviourStat, true);
        upgradeStat = new GuiAnimatedStat(this, "Upgrades", Textures.GUI_UPGRADES_LOCATION, xStart, 3, 0xFF0000FF, infoStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(problemStat);
        animatedStatList.add(redstoneBehaviourStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(upgradeStat);
        redstoneBehaviourStat.setTextWithoutCuttingString(getRedstoneBehaviour());
        infoStat.setText(GuiConstants.INFO_CHARGING_STATION);
        upgradeStat.setText(GuiConstants.UPGRADES_CHARGING_STATION);

        Rectangle buttonRect = redstoneBehaviourStat.getButtonScaledRectangle(xStart - 131, yStart + 30, 130, 20);
        redstoneButton = getButtonFromRectangle(0, buttonRect, "-");
        buttonList.add(redstoneButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){

        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName());

        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2, 6, 4210752);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 28, ySize - 106 + 2, 4210752);
        fontRendererObj.drawString("Upgr.", 46, 19, 4210752);

        switch(te.redstoneMode){
            case 0:
                redstoneButton.displayString = "Never";
                break;
            case 1:
                redstoneButton.displayString = "Done (dis)charging";
                break;
            case 2:
                redstoneButton.displayString = "Charging";
                break;
            case 3:
                redstoneButton.displayString = "Discharging";
                break;
        // case 2: redstoneButton.displayString = "Low Redstone Signal";
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

        renderAir();
        GuiUtils.drawPressureGauge(fontRendererObj, -1, PneumaticValues.MAX_PRESSURE_CHARGING_STATION, PneumaticValues.DANGER_PRESSURE_CHARGING_STATION, -1, te.getPressure(ForgeDirection.UNKNOWN), xStart + xSize * 3 / 4 + 10, yStart + ySize * 1 / 4 + 4, zLevel);

        pressureStat.setText(getPressureStats());
        problemStat.setText(getProblems());
        redstoneButton.visible = redstoneBehaviourStat.isDoneExpanding();

        guiSelectButton.enabled = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) != null && te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).getItem() instanceof IChargingStationGUIHolderItem;
    }

    private List<String> getRedstoneBehaviour(){
        List<String> textList = new ArrayList<String>();
        textList.add("\u00a77Emit redstone if         "); // the spaces are
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
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.VOLUME_CHARGING_STATION) + " mL.");
        float pressureLeft = te.volume - PneumaticValues.VOLUME_CHARGING_STATION;
        if(pressureLeft > 0) {
            pressureStatText.add("\u00a70" + (double)Math.round(pressureLeft) + " mL. (Volume Upgrades)");
            pressureStatText.add("\u00a70--------+");
            pressureStatText.add("\u00a70" + (double)Math.round(te.volume) + " mL.");
        }
        if(te.charging || te.disCharging) {
            pressureStatText.add("\u00a77" + (te.charging ? "C" : "Disc") + "harging at");
            pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.CHARGING_STATION_CHARGE_RATE * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots())) + "mL/tick");
        }

        return pressureStatText;
    }

    private List<String> getProblems(){
        List<String> textList = new ArrayList<String>();
        if(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) == null) {
            textList.add("\u00a77No items to (dis)charge");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.maxCharPerLineLeft));
        } else if(!(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).getItem() instanceof ItemPressurizable)) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be (dis)charged", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.maxCharPerLineLeft));
        } else {
            ItemStack chargeStack = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
            ItemPressurizable chargeItem = (ItemPressurizable)chargeStack.getItem();
            if(chargeItem.getPressure(chargeStack) > te.getPressure(ForgeDirection.UNKNOWN) + 0.01F && chargeStack.getItemDamage() == chargeItem.getMaxDamage()) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be discharged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The item is empty.");
            } else if(chargeItem.getPressure(chargeStack) < te.getPressure(ForgeDirection.UNKNOWN) - 0.01F && chargeStack.getItemDamage() == 0) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be charged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The item is full.");
            } else if(!te.charging && !te.disCharging) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be (dis)charged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The pressures have equalized.");
            }
        }
        if(textList.size() == 0) {
            textList.add("\u00a77No problems");
            textList.add("\u00a70Machine " + (te.charging ? "c" : "disc") + "harging");
        }
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
                if(redstoneBehaviourStat != null) redstoneBehaviourStat.closeWindow();
                break;
        }
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    private void renderAir(){
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glLineWidth(2.0F);
        int particles = 10;
        for(int i = 0; i < particles; i++) {
            renderAirParticle(te.renderAirProgress % (1F / particles) + (float)i / particles);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderAirParticle(float particleProgress){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        float x = xStart + 117F;
        float y = yStart + 50.5F;
        if(particleProgress < 0.5F) {
            y += particleProgress * 56;
        } else if(particleProgress < 0.7F) {
            y += 28F;
            x -= (particleProgress - 0.5F) * 90;
        } else {
            y += 28F;
            x -= 18;
            y -= (particleProgress - 0.7F) * 70;
        }
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.addVertex(x, y, zLevel);
        tess.addVertex(x, y + 1D, zLevel);
        tess.draw();
    }
}
