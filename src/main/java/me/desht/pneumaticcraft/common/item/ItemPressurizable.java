package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.config.ConfigHandler;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPressurizable extends ItemPneumatic implements IPressurizable {
    private final int volume;
    private final float maxPressure;

    public ItemPressurizable(String registryName, int maxAir, int volume) {
        super(registryName);
        setMaxStackSize(1);
        setMaxDamage(maxAir);
        this.volume = volume;
        maxPressure = (float)maxAir / volume;
        setNoRepair();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> par3List) {
        if (isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this, 1, 0);
            ItemStack stack2 = new ItemStack(this, 1, stack.getMaxDamage());
            par3List.add(stack);
            par3List.add(stack2);
        }
    }

    @Override
    public float getPressure(ItemStack iStack) {
        return (float) (iStack.getMaxDamage() - iStack.getItemDamage()) / (float) volume;
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        iStack.setItemDamage(iStack.getItemDamage() - amount);
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

    static void addPressureTooltip(ItemStack stack, List<String> textList) {
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
            textList.add(color + I18n.format("gui.tooltip.pressure", PneumaticCraftUtils.roundNumberTo(p.getPressure(stack), 1)));
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
        if (ConfigHandler.client.alwaysShowPressureDurabilityBar) return true;
        IPressurizable p = IPressurizable.of(stack);
        return p != null && p.getPressure(stack) < p.maxPressure(stack);
    }
}
