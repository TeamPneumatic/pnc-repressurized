package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.common.CommonHUDHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiPneumaticHelmet extends GuiPneumaticInventoryItem{

    private GuiAnimatedStat pressureStat;
    private GuiAnimatedStat statusStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat blockUpgradeStat;
    private GuiAnimatedStat entityUpgradeStat;
    private GuiAnimatedStat searchUpgradeStat;
    private GuiAnimatedStat coordTrackStat;

    public GuiPneumaticHelmet(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container, te);
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        statusStat = new GuiAnimatedStat(this, "Helmet Status", new ItemStack(Itemss.pneumaticHelmet), xStart + xSize, 3, 0xFFFFAA00, pressureStat, false);
        searchUpgradeStat = new GuiAnimatedStat(this, "Item Search Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SEARCH_DAMAGE), xStart + xSize, 3, 0xFF0000FF, statusStat, false);
        coordTrackStat = new GuiAnimatedStat(this, "Coordinate Tracker Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_COORDINATE_TRACKER_DAMAGE), xStart + xSize, 3, 0xFF0000FF, searchUpgradeStat, false);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true);
        entityUpgradeStat = new GuiAnimatedStat(this, "Entity Tracker Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER), xStart, 3, 0xFF0000FF, infoStat, true);
        blockUpgradeStat = new GuiAnimatedStat(this, "Block Tracker Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER), xStart, 3, 0xFF0000FF, entityUpgradeStat, true);
        animatedStatList.add(pressureStat);
        animatedStatList.add(statusStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(searchUpgradeStat);
        animatedStatList.add(coordTrackStat);
        animatedStatList.add(entityUpgradeStat);
        animatedStatList.add(blockUpgradeStat);
        infoStat.setText(GuiConstants.INFO_PNEUMATIC_HELMET);
        searchUpgradeStat.setText(GuiConstants.UPGRADE_PNEUMATIC_HELMET_SEARCHER);
        coordTrackStat.setText(GuiConstants.UPGRADE_PNEUMATIC_HELMET_COORD_TRACKER);
        entityUpgradeStat.setText(GuiConstants.UPGRADE_PNEUMATIC_HELMET_ENTITY_TEXT);
        blockUpgradeStat.setText(GuiConstants.UPGRADE_PNEUMATIC_HELMET_BLOCK_TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y){
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        pressureStat.setText(getPressureStats());
        statusStat.setText(getStatusText());
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        float curPressure = ((IPressurizable)itemStack.getItem()).getPressure(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX));
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * PneumaticValues.PNEUMATIC_HELMET_VOLUME) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(PneumaticValues.PNEUMATIC_HELMET_VOLUME) + " mL.");
        return pressureStatText;
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Air Usage:");
        float totalUsage = UpgradeRenderHandlerList.instance().getAirUsage(FMLClientHandler.instance().getClient().thePlayer);
        if(totalUsage > 0F) {
            EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
            for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
                if(CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersEnabled[i]) {
                    IUpgradeRenderHandler handler = UpgradeRenderHandlerList.instance().upgradeRenderers.get(i);
                    float upgradeUsage = handler.getEnergyUsage(CommonHUDHandler.getHandlerForPlayer(player).rangeUpgradesInstalled, player);
                    if(upgradeUsage > 0F) {
                        text.add(EnumChatFormatting.BLACK.toString() + PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/tick (" + handler.getUpgradeName() + ")");
                    }
                }
            }
            text.add("\u00a70--------+");
            text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(totalUsage, 1) + " mL/tick");
        } else {
            text.add(EnumChatFormatting.BLACK + "0.0 mL/tick");
        }
        text.add("\u00a77Estimated time remaining:");
        int airLeft = (int)(((IPressurizable)itemStack.getItem()).getPressure(itemStack) * PneumaticValues.PNEUMATIC_HELMET_VOLUME);
        if(totalUsage == 0) {
            if(airLeft > 0) text.add("\u00a70infinite");
            else text.add("\u00a700s");
        } else {
            text.add("\u00a70" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds((int)(airLeft / totalUsage), false));
        }
        return text;
    }

    @Override
    public void updateScreen(){
        CommonHUDHandler.getHandlerForPlayer().checkHelmetInventory(itemStack);
        super.updateScreen();
    }
}
