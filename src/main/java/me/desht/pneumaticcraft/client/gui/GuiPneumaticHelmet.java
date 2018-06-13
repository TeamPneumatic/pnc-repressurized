package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.ItemMachineUpgrade;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmorBase;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiPneumaticHelmet extends GuiPneumaticInventoryItem {

    private final String registryName;  // for translation purposes
    private GuiAnimatedStat statusStat;

    public GuiPneumaticHelmet(ContainerChargingStationItemInventory container, TileEntityChargingStation te) {
        super(container, te);
        registryName = itemStack.getItem().getRegistryName().getResourcePath();
    }

    @Override
    public void initGui() {
        super.initGui();
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item." + registryName);
        statusStat = addAnimatedStat("Status", itemStack, 0xFFFFAA00, false);

        Set<Item> upgrades = ((IUpgradeAcceptor)itemStack.getItem()).getApplicableUpgrades();
        boolean leftSided = false;
        for (Item upgrade : upgrades) {
            if (upgrade instanceof ItemMachineUpgrade) {
                addUpgradeStat(((ItemMachineUpgrade) upgrade).getUpgradeType(), leftSided);
                leftSided = !leftSided;
            }
        }

//        addUpgradeStat(EnumUpgrade.SEARCH, false);
//        addUpgradeStat(EnumUpgrade.COORDINATE_TRACKER, false);
//        addUpgradeStat(EnumUpgrade.SECURITY, false);
//        addUpgradeStat(EnumUpgrade.DISPENSER, false);
//        addUpgradeStat(EnumUpgrade.ENTITY_TRACKER, true);
//        addUpgradeStat(EnumUpgrade.BLOCK_TRACKER, true);
//        addUpgradeStat(EnumUpgrade.SPEED, true);
//        addUpgradeStat(EnumUpgrade.VOLUME, true);
//        addUpgradeStat(EnumUpgrade.RANGE, true);
//        if (Loader.isModLoaded(ModIds.THAUMCRAFT)) {
//            addUpgradeStat(EnumUpgrade.THAUMCRAFT, false);
//        }
    }

    private void addUpgradeStat(EnumUpgrade upgrade, boolean leftSided) {
        ItemStack stack = CraftingRegistrator.getUpgrade(upgrade);
        addAnimatedStat(stack.getDisplayName(), stack, 0xFF4040FF, leftSided).setText("gui.tab.info.item.pneumatic_helmet." + upgrade.getName() + "Upgrade");
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        CommonHUDHandler.getHandlerForPlayer().checkHelmetInventory(itemStack);
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        text.add("\u00a77Air Usage:");
        float totalUsage = UpgradeRenderHandlerList.instance().getAirUsage(FMLClientHandler.instance().getClient().player, true);
        if (totalUsage > 0F) {
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            for (int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
                if (CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersInserted[i]) {
                    IUpgradeRenderHandler handler = UpgradeRenderHandlerList.instance().upgradeRenderers.get(i);
                    float upgradeUsage = handler.getEnergyUsage(CommonHUDHandler.getHandlerForPlayer(player).rangeUpgradesInstalled, player);
                    if (upgradeUsage > 0F) {
                        text.add(TextFormatting.BLACK.toString() + PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/tick (" + handler.getUpgradeName() + ")");
                    }
                }
            }
            text.add("\u00a70--------+");
            text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(totalUsage, 1) + " mL/tick");
        } else {
            text.add(TextFormatting.BLACK + "0.0 mL/tick");
        }
        text.add("\u00a77Estimated time remaining:");
        int volume = UpgradableItemUtils.getUpgrades(EnumUpgrade.VOLUME, itemStack) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
        int airLeft = (int) (((IPressurizable) itemStack.getItem()).getPressure(itemStack) * volume);
        if (totalUsage == 0) {
            if (airLeft > 0) text.add("\u00a70infinite");
            else text.add("\u00a700s");
        } else {
            text.add("\u00a70" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds((int) (airLeft / totalUsage), false));
        }
        return text;
    }

    @Override
    protected int getDefaultVolume() {
        return ((ItemPneumaticArmorBase) itemStack.getItem()).getVolume();
//        return PneumaticValues.PNEUMATIC_HELMET_VOLUME;
    }
}
