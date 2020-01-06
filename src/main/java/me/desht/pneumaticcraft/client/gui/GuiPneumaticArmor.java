package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiPneumaticArmor extends GuiPneumaticInventoryItem {

    private final String registryName;  // for translation purposes
    private WidgetAnimatedStat statusStat;
    private final EquipmentSlotType equipmentSlot;

    public GuiPneumaticArmor(ContainerChargingStationItemInventory container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        registryName = itemStack.getItem().getRegistryName().getPath();
        equipmentSlot = ((ArmorItem) itemStack.getItem()).getEquipmentSlot();
    }

    @Override
    public void init() {
        super.init();
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.item." + registryName);
        statusStat = addAnimatedStat("Status", itemStack, 0xFFFFAA00, false);

        List<EnumUpgrade> upgrades = getApplicableUpgrades();
        for (int i = 0; i < upgrades.size(); i++) {
            addUpgradeStat(upgrades.get(i), i <= upgrades.size() / 2);
        }
    }

    private void addUpgradeStat(EnumUpgrade upgrade, boolean leftSided) {
        ItemStack stack = upgrade.getItemStack();
        String key = getTranslationKey(upgrade, equipmentSlot);
        if (!I18n.hasKey(key)) {
            key = getTranslationKey(upgrade, null);
        }
        addAnimatedStat(stack.getDisplayName().getFormattedText(), stack, 0xFF4040FF, leftSided).setText(key);
    }

    private String getTranslationKey(EnumUpgrade upgrade, EquipmentSlotType equipmentSlot) {
        String s = equipmentSlot == null ? "generic" : equipmentSlot.toString().toLowerCase();
        return "gui.tab.info.item.armor." + s + "." + upgrade.getName() + "Upgrade";
    }

    @Override
    public void tick() {
        super.tick();
        CommonArmorHandler.getHandlerForPlayer().initArmorInventory(equipmentSlot);
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        text.add("\u00a77Air Usage:");
        PlayerEntity player = minecraft.player;
        float totalUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, equipmentSlot, true);
        if (totalUsage > 0F) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(equipmentSlot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                if (CommonArmorHandler.getHandlerForPlayer(player).isUpgradeRendererInserted(equipmentSlot, i)) {
                    IUpgradeRenderHandler handler = renderHandlers.get(i);
                    float upgradeUsage = handler.getEnergyUsage(CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(equipmentSlot, EnumUpgrade.RANGE), player);
                    if (upgradeUsage > 0F) {
                        text.add(TextFormatting.BLACK.toString() + PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/tick (" + handler.getUpgradeID() + ")");
                    }
                }
            }
            text.add("\u00a70--------+");
            text.add("\u00a70" + PneumaticCraftUtils.roundNumberTo(totalUsage, 1) + " mL/tick");
        } else {
            text.add(TextFormatting.BLACK + "0.0 mL/tick");
        }
        text.add("\u00a77Estimated time remaining:");
        int volume = UpgradableItemUtils.getUpgrades(itemStack, EnumUpgrade.VOLUME) * PneumaticValues.VOLUME_VOLUME_UPGRADE + getDefaultVolume();
        int airLeft = itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> (int)(h.getPressure() * volume))
                .orElse(0);
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
        return ((ItemPneumaticArmor) itemStack.getItem()).getBaseVolume();
    }
}
