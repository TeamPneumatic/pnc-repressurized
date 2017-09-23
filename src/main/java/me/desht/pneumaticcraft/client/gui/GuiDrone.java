package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.ItemStack;

public class GuiDrone extends GuiPneumaticInventoryItem {

    // private GuiAnimatedStat statusStat;

    public GuiDrone(ContainerChargingStationItemInventory container, TileEntityChargingStation te) {
        super(container, te);
    }

    @Override
    public void initGui() {
        super.initGui();

        // statusStat = new GuiAnimatedStat(this, "Helmet Status", new ItemStack(Items.pneumaticHelmet), xStart + xSize, 3, 0xFFFFAA00, pressureStat, false);
        ItemStack speedUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.SPEED);
        addAnimatedStat(speedUpgrade.getDisplayName(), speedUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.speedUpgrade");
        ItemStack dispenserUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.DISPENSER);
        addAnimatedStat(dispenserUpgrade.getDisplayName(), dispenserUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.dispenserUpgrade");
        ItemStack itemLifeUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.ITEM_LIFE);
        addAnimatedStat(itemLifeUpgrade.getDisplayName(), itemLifeUpgrade, 0xFF0000FF, false).setText("gui.tab.info.item.drone.itemLifeUpgrade");
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item.drone");
        ItemStack securityUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.SECURITY);
        addAnimatedStat(securityUpgrade.getDisplayName(), securityUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.securityUpgrade");
        ItemStack volumeUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.VOLUME);
        addAnimatedStat(volumeUpgrade.getDisplayName(), volumeUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.volumeUpgrade");
        ItemStack entityUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.ENTITY_TRACKER);
        addAnimatedStat(entityUpgrade.getDisplayName(), entityUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.entityUpgrade");
        ItemStack rangeUpgrade = CraftingRegistrator.getUpgrade(EnumUpgrade.RANGE);
        addAnimatedStat(rangeUpgrade.getDisplayName(), rangeUpgrade, 0xFF0000FF, true).setText("gui.tab.info.item.drone.rangeUpgrade");
    }

    @Override
    protected int getDefaultVolume() {
        return PneumaticValues.DRONE_VOLUME;
    }
}
