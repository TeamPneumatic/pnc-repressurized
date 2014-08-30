package pneumaticCraft.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;

public class GuiDrone extends GuiPneumaticInventoryItem{

    private GuiAnimatedStat pressureStat;
    // private GuiAnimatedStat statusStat;
    private GuiAnimatedStat infoStat;
    private GuiAnimatedStat volumeUpgradeStat;
    private GuiAnimatedStat securityUpgradeStat;
    private GuiAnimatedStat speedUpgradeStat;
    private GuiAnimatedStat dispenserUpgradeStat;
    private GuiAnimatedStat itemLifeUpgradeStat;

    public GuiDrone(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container, te);
    }

    @Override
    public void initGui(){
        super.initGui();
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        pressureStat = new GuiAnimatedStat(this, "Pressure", new ItemStack(Blockss.pressureTube), xStart + xSize, yStart + 5, 0xFF00AA00, null, false);
        // statusStat = new GuiAnimatedStat(this, "Helmet Status", new ItemStack(Items.pneumaticHelmet), xStart + xSize, 3, 0xFFFFAA00, pressureStat, false);
        speedUpgradeStat = new GuiAnimatedStat(this, "Speed Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE), xStart + xSize, 3, 0xFF0000FF, pressureStat, false);
        dispenserUpgradeStat = new GuiAnimatedStat(this, "Dispenser Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE), xStart + xSize, 3, 0xFF0000FF, speedUpgradeStat, false);
        itemLifeUpgradeStat = new GuiAnimatedStat(this, "Item Life Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ITEM_LIFE), xStart + xSize, 3, 0xFF0000FF, dispenserUpgradeStat, false);
        infoStat = new GuiAnimatedStat(this, "Information", Textures.GUI_INFO_LOCATION, xStart, yStart + 5, 0xFF8888FF, null, true);
        securityUpgradeStat = new GuiAnimatedStat(this, "Security Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SECURITY), xStart, 3, 0xFF0000FF, infoStat, true);
        volumeUpgradeStat = new GuiAnimatedStat(this, "Volume Upgrade", new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE), xStart, 3, 0xFF0000FF, securityUpgradeStat, true);
        animatedStatList.add(pressureStat);
        // animatedStatList.add(statusStat);
        animatedStatList.add(infoStat);
        animatedStatList.add(speedUpgradeStat);
        animatedStatList.add(dispenserUpgradeStat);
        animatedStatList.add(itemLifeUpgradeStat);
        animatedStatList.add(securityUpgradeStat);
        animatedStatList.add(volumeUpgradeStat);
        infoStat.setText("In this interface you can insert upgrades into the Drone. Open up other tabs to see which enhancements can be made.");
        speedUpgradeStat.setText("Per Speed Upgrade inserted, the moving speed of the Drone will increase by 10%% of it's original moving speed. This can stack up to 10 upgrades, meaning 200% speed is the max.");
        dispenserUpgradeStat.setText("By default the Drone can transfer one stack of items. For every Dispenser Upgrade inserted, the Drone can transfer one additional stack.");
        itemLifeUpgradeStat.setText("With the Item Life Upgrade inserted, the Drone will be able to auto-repair. It will regen health over time. The more upgrades you insert, the stronger the effect.");
        securityUpgradeStat.setText("With one of this upgrade inserted, the Drone will be capable of being immune to liquids. Liquids around the Drone will be removed by the Drone's forcefield.");
        volumeUpgradeStat.setText("For each upgrade inserted, the Drone's volume will be increased by 1000mL. This could be used for long travels.");
    }

    private List<String> getPressureStats(){
        List<String> pressureStatText = new ArrayList<String>();
        pressureStatText.add("\u00a77Current Pressure:");
        float curPressure = ((IPressurizable)itemStack.getItem()).getPressure(te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX));
        int volume = ItemPneumaticArmor.getUpgrades(ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE, te.getStackInSlot(TileEntityChargingStation.CHARGE_INVENTORY_INDEX)) * PneumaticValues.VOLUME_VOLUME_UPGRADE + PneumaticValues.DRONE_VOLUME;
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * 10) / 10 + " bar.");
        pressureStatText.add("\u00a77Current Air:");
        pressureStatText.add("\u00a70" + (double)Math.round(curPressure * volume) + " mL.");
        pressureStatText.add("\u00a77Volume:");
        pressureStatText.add("\u00a70" + (double)Math.round(volume) + " mL.");
        return pressureStatText;
    }

    @Override
    public void updateScreen(){
        pressureStat.setText(getPressureStats());
        //statusStat.setText(getStatusText());
        //CommonHUDHandler.getHandlerForPlayer().checkHelmetInventory(itemStack);
        super.updateScreen();
    }
}
