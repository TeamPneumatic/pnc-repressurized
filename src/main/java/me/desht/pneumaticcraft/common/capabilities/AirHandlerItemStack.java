package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.item.IPressurizableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AirHandlerItemStack implements IAirHandlerItem, ICapabilityProvider {
    public static final String AIR_NBT_KEY = "pneumaticcraft:air";

    private final LazyOptional<IAirHandlerItem> holder = LazyOptional.of(() -> this);

    private final ItemStack container;
    private final IPressurizableItem pressurizable;
    private int baseVolume;
    private final float maxPressure;

    public AirHandlerItemStack(ItemStack container, float maxPressure) {
        Validate.isTrue(container.getItem() instanceof IPressurizableItem, "itemstack " + container + " must be an IPressurizableItem!");
        this.container = container;
        this.pressurizable = (IPressurizableItem) container.getItem();
        this.baseVolume = pressurizable.getBaseVolume();
        this.maxPressure = maxPressure;
    }

    @Nonnull
    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public float getPressure() {
        return pressurizable.getPressure(container);
    }

    @Override
    public int getAir() {
        return pressurizable.getAir(container);
    }

    @Override
    public void addAir(int amount) {
        if (container.getCount() != 1) return;

        int currentAir = getAir();
        int newAir = currentAir + amount;
        if (newAir != 0) {
            container.getOrCreateTag().putInt(AIR_NBT_KEY, currentAir + amount);
        } else {
            // no air in item: clean up NBT for item stackability purposes
            if (container.hasTag()) {
                container.getTag().remove(AIR_NBT_KEY);
                if (container.getTag().isEmpty()) {
                    container.setTag(null);
                }
            }
        }
    }

    @Override
    public int getBaseVolume() {
        return baseVolume;
    }

    @Override
    public void setBaseVolume(int newBaseVolume) {
        this.baseVolume = newBaseVolume;
    }

    @Override
    public int getVolume() {
        return pressurizable.getUpgradedVolume(container);
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
