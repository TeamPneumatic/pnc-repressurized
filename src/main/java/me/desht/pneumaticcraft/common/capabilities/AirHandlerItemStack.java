package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.inventory.handler.ChargeableItemHandler;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AirHandlerItemStack implements IAirHandlerItem, ICapabilityProvider {
    private static final String AIR_NBT_KEY = "pneumaticcraft:air";

    private final LazyOptional<IAirHandlerItem> holder = LazyOptional.of(() -> this);

    private final ItemStack container;
    private final int volume;
    private final float maxPressure;
    private final int nVolumeUpgrades;

    public AirHandlerItemStack(ItemStack container, int volume, float maxPressure) {
        this.container = container;
        this.volume = volume;
        this.maxPressure = maxPressure;
        if (container.hasTag() && container.getTag().contains(ChargeableItemHandler.NBT_UPGRADE_TAG)) {
            this.nVolumeUpgrades = UpgradableItemUtils.getUpgrades(IItemRegistry.EnumUpgrade.VOLUME, container);
        } else {
            this.nVolumeUpgrades = 0;
        }
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
        CompoundNBT tagCompound = container.getTag();
        if (tagCompound == null || !tagCompound.contains(AIR_NBT_KEY)) {
            return 0;
        }
        return tagCompound.getInt(AIR_NBT_KEY);
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
    public int getVolume() {
        return getBaseVolume() + nVolumeUpgrades * PneumaticValues.VOLUME_VOLUME_UPGRADE;
    }

    @Override
    public float maxPressure() {
        return maxPressure;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY.orEmpty(cap, holder);
    }
}
