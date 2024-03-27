/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.upgrademanager;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.inventory.ChargingStationUpgradeManagerMenu;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ArmorItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PneumaticArmorUpgradeScreen extends AbstractUpgradeManagerScreen {

    private final String registryName;  // for translation purposes
    private WidgetAnimatedStat statusStat;
    private final EquipmentSlot equipmentSlot;

    public PneumaticArmorUpgradeScreen(ChargingStationUpgradeManagerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        registryName = PneumaticCraftUtils.getRegistryName(itemStack.getItem()).orElseThrow().getPath();
        equipmentSlot = ((ArmorItem) itemStack.getItem()).getEquipmentSlot();
    }

    @Override
    public void init() {
        super.init();
        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info"), Textures.GUI_INFO_LOCATION, 0xFF8888FF, true)
                .setText(xlate("pneumaticcraft.gui.tab.info.item." + registryName));
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), itemStack, 0xFFFFAA00, false);

        addUpgradeTabs(itemStack.getItem(), "armor." + equipmentSlot.toString().toLowerCase(Locale.ROOT), "armor.generic");
    }

    @Override
    public void containerTick() {
        super.containerTick();
        CommonArmorHandler.getHandlerForPlayer().initArmorInventory(equipmentSlot);
        statusStat.setText(getStatusText());
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();

        ChatFormatting black = ChatFormatting.BLACK;
        text.add(xlate("pneumaticcraft.gui.tab.info.pneumatic_armor.usage").withStyle(ChatFormatting.WHITE));
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        float totalUsage = commonArmorHandler.getIdleAirUsage(equipmentSlot, true);
        if (totalUsage > 0F) {
            List<IArmorUpgradeHandler<?>> handlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(equipmentSlot);
            for (int i = 0; i < handlers.size(); i++) {
                if (commonArmorHandler.isUpgradeInserted(equipmentSlot, i)) {
                    IArmorUpgradeHandler<?> handler = handlers.get(i);
                    float upgradeUsage = handler.getIdleAirUsage(commonArmorHandler);
                    if (upgradeUsage > 0F) {
                        Component desc = xlate(IArmorUpgradeHandler.getStringKey(handler.getID()));
                        Component c = Component.literal(PneumaticCraftUtils.roundNumberTo(upgradeUsage, 1) + " mL/t (")
                                .append(desc)
                                .append(")")
                                .withStyle(black);
                        text.add(c);
                    }
                }
            }
            text.add(Component.literal("--------+").withStyle(black));
            text.add(Component.literal(PneumaticCraftUtils.roundNumberTo(totalUsage, 1) + " mL/t").withStyle(black));
        } else {
            text.add(Component.literal("0.0 mL/t").withStyle(black));
        }
        text.add(xlate("pneumaticcraft.gui.tab.info.pneumatic_armor.timeRemaining").withStyle(ChatFormatting.WHITE));
        int airLeft = PNCCapabilities.getAirHandler(itemStack).orElseThrow().getAir();
        if (totalUsage == 0) {
            if (airLeft > 0) text.add(Component.literal("∞").withStyle(black));
            else text.add(Component.literal("0s").withStyle(black));
        } else {
            text.add(Component.literal(PneumaticCraftUtils.convertTicksToMinutesAndSeconds((int) (airLeft / totalUsage), false)).withStyle(black));
        }
        return text;
    }

    @Override
    protected int getDefaultVolume() {
        return ((PneumaticArmorItem) itemStack.getItem()).getBaseVolume();
    }
}
