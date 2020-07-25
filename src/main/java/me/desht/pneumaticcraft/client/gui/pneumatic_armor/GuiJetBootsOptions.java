package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsUpgradeHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiJetBootsOptions extends GuiSliderOptions<JetBootsUpgradeHandler> {
    private WidgetKeybindCheckBox checkBox;

    public GuiJetBootsOptions(IGuiScreen screen, JetBootsUpgradeHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        checkBox = new WidgetKeybindCheckBox(5, 45, 0xFFFFFFFF, "jetboots.module.builderMode", b -> setBuilderMode(b.checked));
        gui.addWidget(checkBox);

        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getUpgradeHandler(), ArmorHUDLayout.LayoutTypes.JET_BOOTS));
        }));
    }

    private void setBuilderMode(boolean enabled) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsUpgradeHandler.BUILDER_MODE_LEVEL) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean(ItemPneumaticArmor.NBT_BUILDER_MODE, enabled);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.FEET, tag));
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(ItemPneumaticArmor.NBT_BUILDER_MODE, tag.get(ItemPneumaticArmor.NBT_BUILDER_MODE));
            HUDHandler.instance().addFeatureToggleMessage(getUpgradeHandler(), "jetboots.module.builderMode", enabled);
        }
    }

    @Override
    public void tick() {
        super.tick();

        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        checkBox.active = handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsUpgradeHandler.BUILDER_MODE_LEVEL;
    }


    @Override
    protected String getTagName() {
        return ItemPneumaticArmor.NBT_JET_BOOTS_POWER;
    }

    @Override
    protected ITextComponent getPrefix() {
        return new StringTextComponent("Power: ");
    }

    @Override
    protected ITextComponent getSuffix() {
        return new StringTextComponent("%");
    }

}
