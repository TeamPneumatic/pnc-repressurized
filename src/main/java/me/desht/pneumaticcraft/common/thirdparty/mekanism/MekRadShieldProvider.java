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

package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MekRadShieldProvider implements ICapabilityProvider {
    private final IRadiationShielding impl;
    private final LazyOptional<IRadiationShielding> lazy;

    public MekRadShieldProvider(ItemStack stack, EquipmentSlot slot) {
        this.impl = new PneumaticArmorRadiationShield(stack, slot);
        this.lazy = LazyOptional.of(() -> impl);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return MekanismIntegration.CAPABILITY_RADIATION_SHIELDING.orEmpty(cap, lazy);
    }

    public record PneumaticArmorRadiationShield(ItemStack stack, EquipmentSlot slot) implements IRadiationShielding {
        @Override
        public double getRadiationShielding() {
            boolean upgrade = UpgradableItemUtils.getUpgradeCount(stack, ModUpgrades.RADIATION_SHIELDING.get()) > 0;
            if (!upgrade) return 0d;
            return switch (slot) {
                case HEAD -> 0.25;
                case CHEST -> 0.4;
                case LEGS -> 0.2;
                case FEET -> 0.15;
                default -> 0d;
            };
        }
    }
}
