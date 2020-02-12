package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationItemInventory;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
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
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText("gui.tab.info.item." + registryName);
        statusStat = addAnimatedStat("Status", itemStack, 0xFFFFAA00, false);

        addUpgradeTabs(itemStack.getItem(), "armor." + equipmentSlot.toString().toLowerCase(), "armor.generic");
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

        String black = TextFormatting.BLACK.toString();
        text.add(TextFormatting.WHITE + "Air Usage:");
        PlayerEntity player = minecraft.player;
        float totalUsage = UpgradeRenderHandlerList.instance().getAirUsage(player, equipmentSlot, true);
        if (totalUsage > 0F) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(equipmentSlot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                if (CommonArmorHandler.getHandlerForPlayer(player).isUpgradeRendererInserted(equipmentSlot, i)) {
                    IUpgradeRenderHandler handler = renderHandlers.get(i);
                    float upgradeUsage = handler.getEnergyUsage(CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(equipmentSlot, EnumUpgrade.RANGE), player);
                    if (upgradeUsage > 0F) {
                        text.add(black + PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/tick (" + handler.getUpgradeID() + ")");
                    }
                }
            }
            text.add(black + "--------+");
            text.add(black + "" + PneumaticCraftUtils.roundNumberTo(totalUsage, 1) + " mL/tick");
        } else {
            text.add(black + "0.0 mL/tick");
        }
        text.add(TextFormatting.WHITE + "Estimated time remaining:");
        int airLeft = itemStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(IAirHandler::getAir)
                .orElseThrow(RuntimeException::new);
        if (totalUsage == 0) {
            if (airLeft > 0) text.add(black + "infinite");
            else text.add(black + "0s");
        } else {
            text.add(black + "" + PneumaticCraftUtils.convertTicksToMinutesAndSeconds((int) (airLeft / totalUsage), false));
        }
        return text;
    }

    @Override
    protected int getDefaultVolume() {
        return ((ItemPneumaticArmor) itemStack.getItem()).getBaseVolume();
    }
}
