package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemReinforcedAirCanister extends ItemPneumatic implements IPressurizable {
    // we can't just extend ItemPressurizable, because we need to use NBT for the air amount (> 65535)

    private static final String NBT_AIR = "air";
    private static final int MAX_DAMAGE = 250;  // arbitrary

    public ItemReinforcedAirCanister() {
        super("reinforced_air_canister");
        setMaxStackSize(1);
        setMaxDamage(MAX_DAMAGE);
        setNoRepair();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> par3List) {
        if (isInCreativeTab(tab)) {
            ItemStack stack2 = new ItemStack(this);
            addAir(stack2, PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR);
            ItemStack stack = new ItemStack(this);
            addAir(stack, 0);
            par3List.add(stack);
            par3List.add(stack2);
        }
    }

    @Override
    public int getDamage(ItemStack stack) {
        return (int) (MAX_DAMAGE * (getPressure(stack) / maxPressure(stack)));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return ItemPressurizable.shouldShowPressureDurability(stack);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return ItemPressurizable.getDurabilityColor(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0 - getPressure(stack) / maxPressure(stack);
    }

    @Override
    public float getPressure(ItemStack iStack) {
        int currentAir = NBTUtil.getInteger(iStack, NBT_AIR);
        return (float) currentAir / getVolume(iStack);
    }

    @Override
    public void addAir(ItemStack iStack, int amount) {
        int currentAir = NBTUtil.getInteger(iStack, "air");
        NBTUtil.setInteger(iStack, "air",
                MathHelper.clamp(currentAir + amount, 0, PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR));
    }

    @Override
    public float maxPressure(ItemStack iStack) {
        return (float) PneumaticValues.REINFORCED_AIR_CANISTER_MAX_AIR / PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME;
    }

    @Override
    public int getVolume(ItemStack iStack) {
        return PneumaticValues.REINFORCED_AIR_CANISTER_VOLUME;
    }
}
