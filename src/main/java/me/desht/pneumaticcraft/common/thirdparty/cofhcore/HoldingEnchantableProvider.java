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

import cofh.lib.capability.IEnchantableItem;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HoldingEnchantableProvider implements ICapabilityProvider {
    @SuppressWarnings("FieldMayBeFinal")
    @CapabilityInject(IEnchantableItem.class)
    public static Capability<IEnchantableItem> CAPABILITY_ENCHANTABLE_ITEM = null;

    private final AllowHoldingEnchant ench = new AllowHoldingEnchant();
    private final LazyOptional<IEnchantableItem> lazy = LazyOptional.of(() -> ench);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY_ENCHANTABLE_ITEM.orEmpty(cap, lazy);
    }

    public static class AllowHoldingEnchant implements IEnchantableItem {
        @Override
        public boolean supportsEnchantment(Enchantment enchantment) {
            return PNCConfig.Common.Integration.cofhHoldingMultiplier > 0 && enchantment == CoFHCore.holdingEnchantment;
        }
    }
}
