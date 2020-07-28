package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsClientHandler;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JetBootsOptions extends AbstractSliderOptions<JetBootsClientHandler> {
    private WidgetKeybindCheckBox checkBox;

    public JetBootsOptions(IGuiScreen screen, JetBootsClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        checkBox = WidgetKeybindCheckBox.getOrCreate(RL("jet_boots.module.builder_mode"), 5, 45, 0xFFFFFFFF,
                b -> setBuilderMode(b.checked));
        gui.addWidget(checkBox);

        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutTypes.JET_BOOTS));
        }));
    }

    private void setBuilderMode(boolean enabled) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsClientHandler.BUILDER_MODE_LEVEL) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean(ItemPneumaticArmor.NBT_BUILDER_MODE, enabled);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.FEET, tag));
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(ItemPneumaticArmor.NBT_BUILDER_MODE, tag.get(ItemPneumaticArmor.NBT_BUILDER_MODE));
            ResourceLocation id = getClientUpgradeHandler().getCommonHandler().getID();
            HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(id), ArmorUpgradeRegistry.getStringKey(checkBox.getUpgradeId()), enabled);
        }
    }

    @Override
    public void tick() {
        super.tick();

        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        checkBox.active = handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsClientHandler.BUILDER_MODE_LEVEL;
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
