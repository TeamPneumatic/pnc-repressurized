package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.WidgetComboBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.ChargingClientHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ChargeMode;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Arrays;
import java.util.Comparator;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ChargingOptions extends IOptionPage.SimpleOptionPage<ChargingClientHandler> {
    public ChargingOptions(IGuiScreen screen, ChargingClientHandler handler) {
        super(screen, handler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        ChargeMode currentMode = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlot.CHEST)
                .getOrDefault(ModDataComponents.CHARGING_MODE, ChargeMode.ALL);

        int width = Arrays.stream(ChargeMode.values())
                .map(m -> gui.getFontRenderer().width(Component.translatable(m.getTranslationKey())))
                .max(Comparator.naturalOrder())
                .orElse(120);

        gui.addWidget(new WidgetLabel(5, 75, xlate("pneumaticcraft.armor.gui.misc.charge_mode")).setColor(0xFFFFFF));
        gui.addWidget(new WidgetComboBox(Minecraft.getInstance().font, 20, 87, width + 20   , 12,
                b -> PacketUpdateArmorExtraData.sendToServer(
                        getClientUpgradeHandler().getCommonHandler(),
                        ModDataComponents.CHARGING_MODE.get(),
                        ChargeMode.values()[b.getSelectedElementIndex()]
                )
        )).initFromEnum(currentMode);
    }
}
