package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.inventory.ContainerChargingStationUpgradeManager;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiPneumaticArmor extends GuiChargingUpgradeManager {

    private final String registryName;  // for translation purposes
    private WidgetAnimatedStat statusStat;
    private final EquipmentSlotType equipmentSlot;

    public GuiPneumaticArmor(ContainerChargingStationUpgradeManager container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        registryName = itemStack.getItem().getRegistryName().getPath();
        equipmentSlot = ((ArmorItem) itemStack.getItem()).getEquipmentSlot();
    }

    @Override
    public void init() {
        super.init();
        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText("gui.tab.info.item." + registryName);
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), itemStack, 0xFFFFAA00, false);

        addUpgradeTabs(itemStack.getItem(), "armor." + equipmentSlot.toString().toLowerCase(), "armor.generic");
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
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        float totalUsage = commonArmorHandler.getIdleAirUsage(equipmentSlot, true);
        if (totalUsage > 0F) {
            List<IArmorUpgradeHandler> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(equipmentSlot);
            for (int i = 0; i < handlers.size(); i++) {
                if (commonArmorHandler.isUpgradeInserted(equipmentSlot, i)) {
                    IArmorUpgradeHandler handler = handlers.get(i);
                    float upgradeUsage = handler.getIdleAirUsage(commonArmorHandler);
                    if (upgradeUsage > 0F) {
                        text.add(black + PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/tick (" + handler.getID() + ")");
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
