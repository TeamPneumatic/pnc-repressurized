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
import me.desht.pneumaticcraft.api.pneumatic_armor.BuiltinArmorUpgrades;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorExtensionData;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ElytraHandler extends BaseArmorUpgradeHandler<IArmorExtensionData> {

    @Override
    public Identifier getID() {
        return BuiltinArmorUpgrades.ELYTRA;
    }

    @Override
    public PNCUpgrade[] getRequiredUpgrades() {
        return new PNCUpgrade[] { ModUpgrades.ELYTRA.get() };
    }

    @Override
    public float getIdleAirUsage(ICommonArmorHandler armorHandler) {
        return 0;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public void tick(ICommonArmorHandler commonArmorHandler, boolean enabled) {
        Player player = commonArmorHandler.getPlayer();
        if (enabled && !player.level().isClientSide() && player.isFallFlying() && player.getFallFlyingTicks() % 10 == 0) {
            commonArmorHandler.addAir(EquipmentSlot.CHEST, -10);  // 1 air/tick while gliding
        }
    }

    /**
     * Gliding is driven by the vanilla {@code minecraft:glider} item component, so keep that component on the
     * chestplate in step with whether the Elytra upgrade can be used right now (installed, switched on, and
     * enough pressure). Called every tick for the worn chestplate, server-side; the component only actually
     * changes when usability changes.
     *
     * @param handler the player's armor handler
     * @param chestStack the worn chestplate
     */
    public static void updateGlider(CommonArmorHandler handler, ItemStack chestStack) {
        boolean usable = handler.upgradeUsable(CommonUpgradeHandlers.elytraHandler, true);
        if (usable != chestStack.has(DataComponents.GLIDER)) {
            if (usable) {
                chestStack.set(DataComponents.GLIDER, Unit.INSTANCE);
            } else {
                chestStack.remove(DataComponents.GLIDER);
            }
        }
    }

    /**
     * Vanilla damages a glider by 1 point every 20 ticks of flight; with the Elytra upgrade, gliding costs air
     * instead (see {@link #tick(ICommonArmorHandler, boolean)}).
     *
     * @return true if this damage is the vanilla gliding damage for a chestplate with the Elytra upgrade
     */
    public static boolean isGlidingDamage(ItemStack chestStack, LivingEntity entity, int amount) {
        return amount == 1 && chestStack.has(DataComponents.GLIDER) && entity.isFallFlying()
                && (entity.getFallFlyingTicks() + 1) % 20 == 0;
    }
}
