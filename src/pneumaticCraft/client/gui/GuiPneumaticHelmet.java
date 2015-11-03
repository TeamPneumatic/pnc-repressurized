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
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiPneumaticHelmet extends GuiPneumaticInventoryItem{

    private GuiAnimatedStat statusStat;

    public GuiPneumaticHelmet(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container, te);
    }

    @Override
    public void initGui(){
        super.initGui();
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item.pneumaticHelmet");
        statusStat = addAnimatedStat("Helmet Status", new ItemStack(Itemss.pneumaticHelmet), 0xFFFFAA00, false);

        ItemStack searchUpgradeStat = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SEARCH_DAMAGE);
        addAnimatedStat(searchUpgradeStat.getDisplayName(), searchUpgradeStat, 0xFF0000FF, false).setText("gui.tab.info.item.pneumaticHelmet.searchUpgrade");
        ItemStack coordinateTracker = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_COORDINATE_TRACKER_DAMAGE);
        addAnimatedStat(coordinateTracker.getDisplayName(), coordinateTracker, 0xFF0000FF, false).setText("gui.tab.info.item.pneumaticHelmet.coordinateTracker");
        ItemStack entityTracker = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER);
        addAnimatedStat(entityTracker.getDisplayName(), entityTracker, 0xFF0000FF, true).setText("gui.tab.info.item.pneumaticHelmet.entityTracker");
        ItemStack blockTracker = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_BLOCK_TRACKER);
        addAnimatedStat(blockTracker.getDisplayName(), blockTracker, 0xFF0000FF, true).setText("gui.tab.info.item.pneumaticHelmet.blockTracker");
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        CommonHUDHandler.getHandlerForPlayer().checkHelmetInventory(itemStack);
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText(){
        List<String> text = new ArrayList<String>();

        text.add("\u00a77Air Usage:");
        float totalUsage = UpgradeRenderHandlerList.instance().getAirUsage(FMLClientHandler.instance().getClient().thePlayer, true);
        if(totalUsage > 0F) {
            EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;
            for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
                if(CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersInserted[i]) {
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
        int volume = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE, te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
        int airLeft = (int)(((IPressurizable)itemStack.getItem()).getPressure(itemStack) * volume);
        if(totalUsage == 0) {
            if(airLeft > 0) text.add("\u00a70infinite");
            else text.add("\u00a700s");
        } else {
            text.add("\u00a70" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds((int)(airLeft / totalUsage), false));
        }
        return text;
    }

    @Override
    protected int getDefaultVolume(){
        return PneumaticValues.PNEUMATIC_HELMET_VOLUME;
    }
}
