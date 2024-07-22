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

package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ItemVolumeModifier;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class HoldingEnchantableProvider {
    static Holder<Enchantment> holdingEnchantment = null;
    private static final boolean holdingEnabled = ConfigHelper.common().integration.cofhHoldingMultiplier.get() > 0;

    static void registerVolumeModifier() {
        // TODO figure out how to init this now that enchantments are in a datapack registry

        // Gets if Holding enchantment has been registered
        holdingEnchantment = null; // BuiltInRegistries.ENCHANTMENT.get(ResourceLocation.fromNamespaceAndPath(ModIds.COFH_CORE, "holding"));

        // Registers the volume modifier for the Holding enchant if it's present and config-enabled
        if (holdingEnchantment != null && holdingEnabled) {
            PneumaticRegistry.getInstance().getItemRegistry().registerPneumaticVolumeModifier(new C0FHVolumeModifier(holdingEnchantment));
        }
    }

    public record C0FHVolumeModifier(Holder<Enchantment> holding) implements ItemVolumeModifier {
        @Override
        public int getNewVolume(ItemStack stack, int oldVolume) {
            // finalVolume = baseVolume * ((1 + level_of_holding_enchantment) * configMultiplier)
            return (int)Math.ceil(oldVolume * ((1 + stack.getEnchantmentLevel(holding))
                    * ConfigHelper.common().integration.cofhHoldingMultiplier.get()));
        }

        @Override
        public void addInfo(ItemStack stack, List<Component> text) {
            int count = stack.getEnchantmentLevel(holding);
            if (count > 0) {
                text.add(Component.literal(Symbols.TRIANGLE_RIGHT + " ").append(Enchantment.getFullname(holding, count)));
            }
        }
    }
}
