package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsUpgradeHandler;
import me.desht.pneumaticcraft.common.config.aux.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;

public class GuiJetBootsOptions extends IOptionPage.SimpleToggleableOptions<JetBootsUpgradeHandler> {
    private WidgetKeybindCheckBox checkBox;

    public GuiJetBootsOptions(IGuiScreen screen, JetBootsUpgradeHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        checkBox = new WidgetKeybindCheckBox(5, 45, 0xFFFFFFFF, "jetboots.module.builderMode", b -> setBuilderMode(b.checked));
        gui.addWidget(checkBox);

        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20, "Move Stat Screen...", b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.JET_BOOTS));
        }));
    }

    private void setBuilderMode(boolean enabled) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) >= 8) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean(ItemPneumaticArmor.NBT_BUILDER_MODE, enabled);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.FEET, tag));
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(EquipmentSlotType.FEET, ItemPneumaticArmor.NBT_BUILDER_MODE, tag.get(ItemPneumaticArmor.NBT_BUILDER_MODE));
            HUDHandler.instance().addFeatureToggleMessage(getUpgradeHandler(), "jetboots.module.builderMode", enabled);
        }
    }

    public void tick() {
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        checkBox.enabled = handler.getUpgradeCount(EquipmentSlotType.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) >= 8;
    }
}
