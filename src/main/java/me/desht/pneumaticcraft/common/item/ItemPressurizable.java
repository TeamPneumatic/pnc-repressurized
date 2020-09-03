package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemPressurizable extends Item {

    private final int volume;
    private final float maxPressure;

    public ItemPressurizable(int maxAir, int volume) {
        this(ModItems.defaultProps(), maxAir, volume);
    }

    public ItemPressurizable(Item.Properties props, int maxAir, int volume) {
        super(props);

        this.volume = volume;
        this.maxPressure = (float) maxAir / volume;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return shouldShowPressureDurability(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return getPressureDurabilityColor(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> 1 - (h.getPressure() / h.maxPressure()))
                .orElse(1f);
    }

    static int getPressureDurabilityColor(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            int c = (int) (64 + 191 * f);
            return 0x40 << 16 | c << 8 | 0xFF;
        }).orElse(0xC0C0C0);
    }

    static boolean shouldShowPressureDurability(ItemStack stack) {
                return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY)
                .map(airHandler -> airHandler.getPressure() < airHandler.maxPressure())
                .orElse(false);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        // too early to use a capability here :(
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));

            ItemStack stack = new ItemStack(this);
            new AirHandlerItemStack(stack, volume, maxPressure).addAir((int) (volume * maxPressure));
            items.add(stack);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new AirHandlerItemStack(stack, volume, maxPressure);
    }

    protected float getPressure(ItemStack stack) {
        return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new).getPressure();
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return roundedPressure(stack);
    }

    public static int getAir(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            return tag.getInt(AirHandlerItemStack.AIR_NBT_KEY);
        }
        return 0;
    }

    /**
     * Get an ItemStack's NBT, rounding its air level for sync to client.
     * Default precision of volume/10 is enough precision to display 1 decimal place of pressure,
     * and will greatly reduce server->client chatter
     * @param stack the itemstack being sync'd
     * @return the item's NBT, but with the air level rounded
     */
    public static CompoundNBT roundedPressure(ItemStack stack) {
        CompoundNBT tag = stack.getTag();

        if (tag != null && tag.contains(AirHandlerItemStack.AIR_NBT_KEY)) {
            return stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).map(h -> {
                CompoundNBT tag2 = tag.copy();
                int air = tag2.getInt(AirHandlerItemStack.AIR_NBT_KEY);
                tag2.putInt(AirHandlerItemStack.AIR_NBT_KEY, air - air % (h.getVolume() / PNCConfig.Common.Advanced.pressureSyncPrecision));
                return tag2;
            }).orElseThrow(RuntimeException::new);
        } else {
            return tag;
        }
    }
}
