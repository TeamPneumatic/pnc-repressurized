package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AirHandlerItemStack extends IAirHandlerItem.Provider {
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
        float pressure = pressurizable.getPressure(container);
        if (pressure > maxPressure) {
            // this isn't impossible, e.g. enchant an item with CoFH Holding, pressurize, then disenchant...
            // best option in this case is just to reduce air to the max amount it can actually hold
            container.getOrCreateTag().putInt(AIR_NBT_KEY, (int) (maxPressure * getVolume()));
            return maxPressure;
        }
        return pressure;
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
        return pressurizable.getEffectiveVolume(container);
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
