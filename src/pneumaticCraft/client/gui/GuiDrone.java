package pneumaticCraft.client.gui;

import net.minecraft.item.ItemStack;
import pneumaticCraft.common.inventory.ContainerChargingStationItemInventory;
import pneumaticCraft.common.item.ItemMachineUpgrade;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityChargingStation;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.lib.Textures;

public class GuiDrone extends GuiPneumaticInventoryItem{

    // private GuiAnimatedStat statusStat;

    public GuiDrone(ContainerChargingStationItemInventory container, TileEntityChargingStation te){
        super(container, te);
    }

    @Override
    public void initGui(){
        super.initGui();

        // statusStat = new GuiAnimatedStat(this, "Helmet Status", new ItemStack(Items.pneumaticHelmet), xStart + xSize, 3, 0xFFFFAA00, pressureStat, false);
        ItemStack speedUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SPEED_DAMAGE);
        addAnimatedStat(speedUpgrade.getDisplayName(), speedUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.speedUpgrade");
        ItemStack dispenserUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_DISPENSER_DAMAGE);
        addAnimatedStat(dispenserUpgrade.getDisplayName(), dispenserUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.dispenserUpgrade");
        ItemStack itemLifeUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ITEM_LIFE);
        addAnimatedStat(itemLifeUpgrade.getDisplayName(), itemLifeUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.itemLifeUpgrade");
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item.drone");
        ItemStack securityUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_SECURITY);
        addAnimatedStat(securityUpgrade.getDisplayName(), securityUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.securityUpgrade");
        ItemStack volumeUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_VOLUME_DAMAGE);
        addAnimatedStat(volumeUpgrade.getDisplayName(), volumeUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.volumeUpgrade");
        ItemStack entityUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_ENTITY_TRACKER);
        addAnimatedStat(entityUpgrade.getDisplayName(), entityUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.entityUpgrade");
        ItemStack rangeUpgrade = new ItemStack(Itemss.machineUpgrade, 1, ItemMachineUpgrade.UPGRADE_RANGE);
        addAnimatedStat(rangeUpgrade.getDisplayName(), rangeUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.rangeUpgrade");
    }

    @Override
    protected int getDefaultVolume(){
        return PneumaticValues.DRONE_VOLUME;
    }
}
