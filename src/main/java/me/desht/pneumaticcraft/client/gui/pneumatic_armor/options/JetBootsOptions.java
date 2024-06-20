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
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IKeybindingButton;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.JetBootsClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.network.PacketUpdateArmorExtraData;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JetBootsHandler;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Optional;

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

        IClientArmorRegistry registry = PneumaticRegistry.getInstance().getClientArmorRegistry();
        ResourceLocation ownerID = getClientUpgradeHandler().getID();
        checkBoxBuilderMode = registry.makeKeybindingCheckBox(JetBootsClientHandler.MODULE_BUILDER_MODE, 5, 60, 0xFFFFFFFF,
                b -> setFlag(ModDataComponents.JET_BOOTS_BUILDER_MODE.get(), JetBootsHandler.BUILDER_MODE_LEVEL, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(checkBoxBuilderMode.asWidget());
        checkBoxStabilizers = registry.makeKeybindingCheckBox(JetBootsClientHandler.MODULE_FLIGHT_STABILIZERS, 5, 75, 0xFFFFFFFF,
                b -> setFlag(ModDataComponents.JET_BOOTS_STABILIZERS.get(), JetBootsHandler.STABILIZERS_LEVEL, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(checkBoxStabilizers.asWidget());
        ICheckboxWidget hover = registry.makeKeybindingCheckBox(JetBootsClientHandler.MODULE_HOVER, 5, 90, 0xFFFFFFFF,
                        b -> setFlag(ModDataComponents.JET_BOOTS_HOVER.get(), 1, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(hover.asWidget());
        ICheckboxWidget smartHover = registry.makeKeybindingCheckBox(JetBootsClientHandler.MODULE_SMART_HOVER, 5, 105, 0xFFFFFFFF,
                b -> setFlag(ModDataComponents.JET_BOOTS_SMART_HOVER.get(), 1, b))
                .withOwnerUpgradeID(ownerID);
        gui.addWidget(smartHover.asWidget());

        changeKeybindingButton = registry.makeKeybindingButton(155, KeyHandler.getInstance().keybindJetBoots);
        gui.addWidget(changeKeybindingButton.asWidget());

        gui.addWidget(ClientArmorRegistry.getInstance().makeStatMoveButton(30, 177, getClientUpgradeHandler()));
    }

    @Override
    protected PointXY getSliderPos() {
        return new PointXY(30, 125);
    }

    @Override
    protected DataComponentType<Integer> getIntegerComponent() {
        return ModDataComponents.JET_BOOTS_PCT.get();
    }

    private void setFlag(DataComponentType<Boolean> componentType, int minTier, ICheckboxWidget cb) {
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        if (commonArmorHandler.getUpgradeCount(EquipmentSlot.FEET, ModUpgrades.JET_BOOTS.get()) >= minTier) {
            JetBootsHandler upgradeHandler = getClientUpgradeHandler().getCommonHandler();
            PacketUpdateArmorExtraData.sendToServer(upgradeHandler, componentType, cb.isChecked());
            upgradeHandler.onDataFieldUpdated(CommonArmorHandler.getHandlerForPlayer(), componentType, cb.isChecked());

            ResourceLocation ownerId = upgradeHandler.getID();
            HUDHandler.getInstance().addFeatureToggleMessage(
                    IArmorUpgradeHandler.getStringKey(ownerId), IArmorUpgradeHandler.getStringKey(cb.getUpgradeId()), cb.isChecked()
            );
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
    protected Component getPrefix() {
        return Component.literal("Power: ");
    }

    @Override
    protected Component getSuffix() {
        return Component.literal("%");
    }

    @Override
    public Optional<IKeybindingButton> getKeybindingButton() {
        return Optional.of(changeKeybindingButton);
    }
}
