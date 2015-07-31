package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.common.inventory.ContainerChargingStation;
import pneumaticCraft.common.item.IChargingStationGUIHolderItem;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChargingStation extends GuiPneumaticContainerBase<TileEntityChargingStation>{
    private GuiButton guiSelectButton;

    public GuiChargingStation(InventoryPlayer player, TileEntityChargingStation te){

        super(new ContainerChargingStation(player, te), te, Textures.GUI_CHARGING_STATION_LOCATION);
    }

    @Override
    public void initGui(){
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        guiSelectButton = new GuiButton(1, xStart + 90, yStart + 15, 25, 20, "inv.");
        buttonList.add(guiSelectButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 46, 19, 4210752);
    }

    @Override
    protected Point getInvTextOffset(){
        return new Point(20, 0);
    }

    @Override
    public String getRedstoneButtonText(int mode){
        switch(mode){
            case 0:
                return "gui.tab.redstoneBehaviour.button.never";
            case 1:
                return "gui.tab.redstoneBehaviour.chargingStation.button.doneDischarging";
            case 2:
                return "gui.tab.redstoneBehaviour.chargingStation.button.charging";
            case 3:
                return "gui.tab.redstoneBehaviour.chargingStation.button.discharging";

        }
        return "<ERROR>";
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        renderAir();
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        guiSelectButton.enabled = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) != null && te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).getItem() instanceof IChargingStationGUIHolderItem;

    }

    @Override
    protected Point getGaugeLocation(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 10, yStart + ySize * 1 / 4 + 4);
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        if(te.charging || te.disCharging) {
            pressureStatText.add("\u00a77" + (te.charging ? "C" : "Disc") + "harging at");
            pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.CHARGING_STATION_CHARGE_RATE * te.getSpeedMultiplierFromUpgrades(te.getUpgradeSlots())) + "mL/tick");
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX) == null) {
            textList.add("\u00a77No items to (dis)charge");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.maxCharPerLineLeft));
        } else if(!(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX).getItem() instanceof IPressurizable)) {
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be (dis)charged", GuiConstants.maxCharPerLineLeft));
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put a pneumatic item in the charge slot.", GuiConstants.maxCharPerLineLeft));
        } else {
            ItemStack chargeStack = te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX);
            IPressurizable chargeItem = (IPressurizable)chargeStack.getItem();
            if(chargeItem.getPressure(chargeStack) > te.getPressure(ForgeDirection.UNKNOWN) + 0.01F && chargeItem.getPressure(chargeStack) <= 0) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be discharged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The item is empty.");
            } else if(chargeItem.getPressure(chargeStack) < te.getPressure(ForgeDirection.UNKNOWN) - 0.01F && chargeItem.getPressure(chargeStack) >= chargeItem.maxPressure(chargeStack)) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be charged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The item is full.");
            } else if(!te.charging && !te.disCharging) {
                textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a77The put in item can't be (dis)charged", GuiConstants.maxCharPerLineLeft));
                textList.add("\u00a70The pressures have equalized.");
            }
        }
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
