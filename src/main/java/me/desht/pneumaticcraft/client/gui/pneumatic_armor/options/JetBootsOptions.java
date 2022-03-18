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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.ICheckboxWidget;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IPneumaticHelmetRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorStatMoveScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.JetBootsClientHandler;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JetBootsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Optional;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class JetBootsOptions extends AbstractSliderOptions<JetBootsClientHandler> {
    private ICheckboxWidget checkBoxBuilderMode;
    private ICheckboxWidget checkBoxStabilizers;
    private IKeybindingButton changeKeybindingButton;

    public JetBootsOptions(IGuiScreen screen, JetBootsClientHandler upgradeHandler) {
        super(screen, upgradeHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        super.populateGui(gui);

        IPneumaticHelmetRegistry registry = PneumaticRegistry.getInstance().getHelmetRegistry();
        ResourceLocation ownerID = getClientUpgradeHandler().getCommonHandler().getID();
        checkBoxBuilderMode = registry.makeKeybindingCheckBox(RL("jet_boots.module.builder_mode"), 5, 45, 0xFFFFFFFF,
                b -> setFlag(PneumaticArmorItem.NBT_BUILDER_MODE, JetBootsHandler.BUILDER_MODE_LEVEL, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(checkBoxBuilderMode.asWidget());
        checkBoxStabilizers = registry.makeKeybindingCheckBox(RL("jet_boots.module.flight_stabilizers"), 5, 65, 0xFFFFFFFF,
                b -> setFlag(PneumaticArmorItem.NBT_FLIGHT_STABILIZERS, JetBootsHandler.STABILIZERS_LEVEL, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(checkBoxStabilizers.asWidget());
        ICheckboxWidget hoverControl = registry.makeKeybindingCheckBox(RL("jet_boots.module.smart_hover"), 5, 85, 0xFFFFFFFF,
                b -> setFlag(PneumaticArmorItem.NBT_SMART_HOVER, 1, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(hoverControl.asWidget());

        changeKeybindingButton = registry.makeKeybindingButton(135, KeyHandler.getInstance().keybindJetBoots);
        gui.addWidget(changeKeybindingButton.asWidget());

        gui.addWidget(new WidgetButtonExtended(30, 157, 150, 20,
                xlate("pneumaticcraft.armor.gui.misc.moveStatScreen"), b -> {
            Minecraft.getInstance().player.closeContainer();
            Minecraft.getInstance().setScreen(new ArmorStatMoveScreen(getClientUpgradeHandler(), ArmorHUDLayout.LayoutType.JET_BOOTS));
        }));
    }

    @Override
    protected PointXY getSliderPos() {
        return new PointXY(30, 105);
    }

    private void setFlag(String flagName, int minTier, ICheckboxWidget cb) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()) >= minTier) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(flagName, cb.isChecked());
            JetBootsHandler upgradeHandler = getClientUpgradeHandler().getCommonHandler();
            NetworkHandler.sendToServer(new PacketUpdateArmorExtraData(EquipmentSlot.FEET, tag, upgradeHandler.getID()));
            upgradeHandler.onDataFieldUpdated(CommonArmorHandler.getHandlerForPlayer(), flagName, tag.get(flagName));
            ResourceLocation ownerId = upgradeHandler.getID();
            HUDHandler.getInstance().addFeatureToggleMessage(IArmorUpgradeHandler.getStringKey(ownerId), IArmorUpgradeHandler.getStringKey(cb.getUpgradeId()), cb.isChecked());
        }
    }

    @Override
    public void tick() {
        super.tick();

        int nUpgrades = CommonArmorHandler.getHandlerForPlayer().getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get());
        checkBoxBuilderMode.asWidget().active = nUpgrades >= JetBootsHandler.BUILDER_MODE_LEVEL;
        checkBoxStabilizers.asWidget().active = nUpgrades >= JetBootsHandler.STABILIZERS_LEVEL;
    }

    @Override
    protected String getTagName() {
        return PneumaticArmorItem.NBT_JET_BOOTS_POWER;
    }

    @Override
    protected Component getPrefix() {
        return new TextComponent("Power: ");
    }

    @Override
    protected Component getSuffix() {
        return new TextComponent("%");
    }

    @Override
    public Optional<IKeybindingButton> getKeybindingButton() {
        return Optional.of(changeKeybindingButton);
    }
}
