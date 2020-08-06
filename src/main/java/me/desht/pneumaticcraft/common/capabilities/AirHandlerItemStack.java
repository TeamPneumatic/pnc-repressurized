package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.item.ItemPressurizable;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AirHandlerItemStack implements IAirHandlerItem, ICapabilityProvider {
    public static final String AIR_NBT_KEY = "pneumaticcraft:air";

    private final LazyOptional<IAirHandlerItem> holder = LazyOptional.of(() -> this);

    private final ItemStack container;
    private int volume;
    private final float maxPressure;

    public AirHandlerItemStack(ItemStack container, int volume, float maxPressure) {
        this.container = container;
        this.volume = volume;
        this.maxPressure = maxPressure;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public float getPressure() {
        return (float) getAir() / getVolume();
    }

    @Override
    public int getAir() {
        return ItemPressurizable.getAir(container);
    }

    @Override
    public void addAir(int amount) {
        int currentAir = getAir();
        container.getOrCreateTag().putInt(AIR_NBT_KEY, currentAir + amount);
    }

    @Override
    public int getBaseVolume() {
        return volume;
    }

    @Override
    public void setBaseVolume(int newBaseVolume) {
        this.volume = newBaseVolume;
    }

    @Override
    public int getVolume() {
        int nUpgrades = UpgradableItemUtils.getUpgrades(container, EnumUpgrade.VOLUME);
        return ApplicableUpgradesDB.getInstance().getUpgradedVolume(getBaseVolume(), nUpgrades);
    }

    @Override
    public float maxPressure() {
        return maxPressure;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY.orEmpty(cap, holder);
    }
}
