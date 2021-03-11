package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import mekanism.common.lib.radiation.capability.IRadiationShielding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MekRadShieldProvider implements ICapabilityProvider {
    private final IRadiationShielding impl;
    private final LazyOptional<IRadiationShielding> lazy;

    public MekRadShieldProvider(ItemStack stack, EquipmentSlotType slot) {
        this.impl = new PneumaticArmorRadiationShield(stack, slot);
        this.lazy = LazyOptional.of(() -> impl);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return MekanismIntegration.CAPABILITY_RADIATION_SHIELDING.orEmpty(cap, lazy);
    }

    public static class PneumaticArmorRadiationShield implements IRadiationShielding {
        private final ItemStack stack;
        private final EquipmentSlotType slot;

        public PneumaticArmorRadiationShield(ItemStack stack, EquipmentSlotType slot) {
            this.stack = stack;
            this.slot = slot;
        }

        @Override
        public double getRadiationShielding() {
            boolean upgrade = UpgradableItemUtils.getUpgrades(stack, EnumUpgrade.RADIATION_SHIELDING) > 0;
            if (!upgrade) return 0d;
            switch (slot) {
                case HEAD: return 0.25;
                case CHEST: return 0.4;
                case LEGS: return 0.2;
                case FEET: return 0.15;
                default: return 0d;
            }
        }
    }
}
