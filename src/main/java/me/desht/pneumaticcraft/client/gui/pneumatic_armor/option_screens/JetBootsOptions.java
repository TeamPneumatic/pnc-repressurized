package me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiMoveStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsClientHandler;
import me.desht.pneumaticcraft.client.util.PointXY;
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
    private WidgetKeybindCheckBox checkBoxBuilderMode;
    private WidgetKeybindCheckBox checkBoxStabilizers;

    public JetBootsOptions(IGuiScreen screen, JetBootsClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        checkBoxBuilderMode = WidgetKeybindCheckBox.getOrCreate(RL("jet_boots.module.builder_mode"), 5, 45, 0xFFFFFFFF,
                b -> setFlag(ItemPneumaticArmor.NBT_BUILDER_MODE, JetBootsClientHandler.BUILDER_MODE_LEVEL, (WidgetKeybindCheckBox) b))
                .withOwnerUpgradeID(getClientUpgradeHandler().getCommonHandler().getID());
        gui.addWidget(checkBoxBuilderMode);
        checkBoxStabilizers = WidgetKeybindCheckBox.getOrCreate(RL("jet_boots.module.flight_stabilizers"), 5, 65, 0xFFFFFFFF,
                b -> setFlag(ItemPneumaticArmor.NBT_FLIGHT_STABILIZERS, JetBootsClientHandler.STABLIZERS_LEVEL, (WidgetKeybindCheckBox) b))
                .withOwnerUpgradeID(getClientUpgradeHandler().getCommonHandler().getID());
        gui.addWidget(checkBoxStabilizers);

        gui.addWidget(new WidgetButtonExtended(30, 128, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeScreen();
            Minecraft.getInstance().displayGuiScreen(new GuiMoveStat(getClientUpgradeHandler(), ArmorHUDLayout.LayoutTypes.JET_BOOTS));
        }));
    }

    @Override
    protected PointXY getSliderPos() {
        return new PointXY(30, 80);
    }

    private void setFlag(String flagName, int minTier, WidgetKeybindCheckBox cb) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= minTier) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean(flagName, cb.checked);
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlotType.FEET, tag));
            CommonArmorHandler.getHandlerForPlayer().onDataFieldUpdated(flagName, tag.get(flagName));
            ResourceLocation ownerId = getClientUpgradeHandler().getCommonHandler().getID();
            HUDHandler.getInstance().addFeatureToggleMessage(ArmorUpgradeRegistry.getStringKey(ownerId), ArmorUpgradeRegistry.getStringKey(cb.getUpgradeId()), cb.checked);
        }
    }

    @Override
    public void tick() {
        super.tick();

        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        checkBoxBuilderMode.active = handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsClientHandler.BUILDER_MODE_LEVEL;
        checkBoxStabilizers.active = handler.getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.JET_BOOTS) >= JetBootsClientHandler.STABLIZERS_LEVEL;
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
