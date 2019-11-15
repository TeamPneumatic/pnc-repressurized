package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class ItemReinforcedAirCanister extends ItemPneumatic implements IPressurizable {
    // we can't just extend ItemPressurizable, because we need to use NBT for the air amount (> 65535)

    private static final String NBT_AIR = "air";
    private static final int MAX_DAMAGE = 250;  // arbitrary

    public ItemReinforcedAirCanister() {
        super(defaultProps().maxDamage(MAX_DAMAGE).setNoRepair(), "reinforced_air_canister");
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
        int currentAir = NBTUtil.getInteger(iStack, NBT_AIR);
        NBTUtil.setInteger(iStack, NBT_AIR,
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
