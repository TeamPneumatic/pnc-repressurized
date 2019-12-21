package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.capabilities.CapabilityAirHandler;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemPressurizable extends ItemPneumatic {
    private final int volume;
    private final float maxPressure;

    public ItemPressurizable(String registryName, int maxAir, int volume) {
        this(defaultProps(), registryName, maxAir, volume);
    }

    public ItemPressurizable(Item.Properties props, String registryName, int maxAir, int volume) {
        super(props, registryName);

        this.volume = volume;
        this.maxPressure = (float)maxAir / volume;
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
        return stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY)
                .map(h -> 1 - (h.getPressure() / h.maxPressure()))
                .orElse(1f);
    }

    static void addPressureTooltip(ItemStack stack, List<ITextComponent> textList) {
        stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            TextFormatting color;
            if (f < 0.1f) {
                color = TextFormatting.RED;
            } else if (f < 0.5f) {
                color = TextFormatting.GOLD;
            } else {
                color = TextFormatting.DARK_GREEN;
            }
            textList.add(xlate("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(airHandler.getPressure(), 1)).applyTextStyle(color));
        });
    }

    public static int getPressureDurabilityColor(ItemStack stack) {
        return stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY).map(airHandler -> {
            float f = airHandler.getPressure() / airHandler.maxPressure();
            int c = (int) (64 + 191 * f);
            return 0x40 << 16 | c << 8 | 0xFF;
        }).orElse(0xC0C0C0);
    }

    public static boolean shouldShowPressureDurability(ItemStack stack) {
        if (PNCConfig.Client.alwaysShowPressureDurabilityBar) return true;

        return stack.getCapability(CapabilityAirHandler.AIR_HANDLER_ITEM_CAPABILITY)
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
        if (this instanceof ItemPressurizable) {
            return new AirHandlerItemStack(stack, volume, maxPressure);
        } else {
            return super.initCapabilities(stack, nbt);
        }
    }
}
