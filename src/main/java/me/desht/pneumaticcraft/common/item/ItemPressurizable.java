package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemPressurizable extends ItemPneumatic implements IPressurizable {
    private final int volume;
    private final float maxPressure;

    public ItemPressurizable(String registryName, int maxAir, int volume) {
        this(defaultProps(), registryName, maxAir, volume);
    }

    public ItemPressurizable(Item.Properties props, String registryName, int maxAir, int volume) {
        super(props.setNoRepair().defaultMaxDamage(maxAir), registryName);
        this.volume = volume;
        maxPressure = (float)maxAir / volume;
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> par3List) {
//        if (isInCreativeTab(tab)) {
//            ItemStack stack = new ItemStack(this, 1, 0);
//            ItemStack stack2 = new ItemStack(this, 1, stack.getMaxDamage());
//            par3List.add(stack);
//            par3List.add(stack2);
//        }
//    }

    @Override
    public float getPressure(ItemStack iStack) {
        return (float) (iStack.getMaxDamage() - iStack.getDamage()) / (float) volume;
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        iStack.setDamage(iStack.getDamage() - amount);
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return maxPressure;
    }

    @Override
    public int getVolume(ItemStack itemStack) {
        // note: no volume upgrade support by default
        return volume;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return shouldShowPressureDurability(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return getDurabilityColor(stack);
    }

    static void addPressureTooltip(ItemStack stack, List<ITextComponent> textList) {
        IPressurizable p = IPressurizable.of(stack);
        if (p != null) {
            float f = p.getPressure(stack) / p.maxPressure(stack);
            TextFormatting color;
            if (f < 0.1f) {
                color = TextFormatting.RED;
            } else if (f < 0.5f) {
                color = TextFormatting.GOLD;
            } else {
                color = TextFormatting.DARK_GREEN;
            }
            textList.add(xlate("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(p.getPressure(stack), 1)).applyTextStyle(color));
        }
    }

    public static int getDurabilityColor(ItemStack stack) {
        IPressurizable p = IPressurizable.of(stack);
        if (p != null) {
            float f = p.getPressure(stack) / p.maxPressure(stack);
            int c = (int) (64 + 191 * f);
            return 0x40 << 16 | c << 8 | 0xFF;
        }
        return 0xC0C0C0;
    }

    public static boolean shouldShowPressureDurability(ItemStack stack) {
        if (PNCConfig.Client.alwaysShowPressureDurabilityBar) return true;
        IPressurizable p = IPressurizable.of(stack);
        return p != null && p.getPressure(stack) < p.maxPressure(stack);
    }
}
