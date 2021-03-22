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
    @CapabilityInject(IEnchantableItem.class)
    public static final Capability<IEnchantableItem> CAPABILITY_ENCHANTABLE_ITEM = null;

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
