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

package me.desht.pneumaticcraft.common.pneumatic_armor.handlers;

import me.desht.pneumaticcraft.api.pneumatic_armor.BaseArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class StepAssistHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {
    private static final UUID STEP_ASSIST_MODIFIER_ID = UUID.fromString("30bc8c6e-4f40-41e5-8b11-4001a9a85afb");

    private static final ResourceLocation ID = RL("step_assist");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[0];  // no upgrades needed, boots built-in
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.FEET;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        AttributeInstance attributeInstance = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attributeInstance != null) {
            AttributeModifier currentModifier = attributeInstance.getModifier(STEP_ASSIST_MODIFIER_ID);
            double stepBoost = enabled && commonArmorHandler.hasMinPressure(EquipmentSlot.FEET) && !player.isShiftKeyDown() ? 0.6 : 0f;
            if (currentModifier != null) {
                if (PneumaticCraftUtils.epsilonEquals(currentModifier.getAmount(), stepBoost)) {
                    return;  // already good
                }
                attributeInstance.removeModifier(currentModifier);
            }
            if (stepBoost > 0) {
                attributeInstance.addTransientModifier(new AttributeModifier(STEP_ASSIST_MODIFIER_ID, "Step Assist", stepBoost, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @Override
    public void onToggle(ICommonArmorHandler commonArmorHandler, boolean newState) {
        if (!newState) {
            AttributeInstance attributeInstance = commonArmorHandler.getPlayer().getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
            if (attributeInstance != null) {
                AttributeModifier currentModifier = attributeInstance.getModifier(STEP_ASSIST_MODIFIER_ID);
                if (currentModifier != null) attributeInstance.removeModifier(currentModifier);
            }
        }
    }

    @Override
    public void onShutdown(ICommonArmorHandler commonArmorHandler) {
        AttributeInstance attributeInstance = commonArmorHandler.getPlayer().getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attributeInstance != null) {
            AttributeModifier currentModifier = attributeInstance.getModifier(STEP_ASSIST_MODIFIER_ID);
            if (currentModifier != null) attributeInstance.removeModifier(currentModifier);
        }
    }
}
