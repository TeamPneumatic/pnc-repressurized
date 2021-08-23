package me.desht.pneumaticcraft.common.core;

import net.minecraft.item.Food;

public class ModFoods {
    public static final Food SOURDOUGH = new Food.Builder().nutrition(7).saturationMod(1.0f).build();
    public static final Food CHIPS = new Food.Builder().nutrition(5).saturationMod(0.6f).build();
    public static final Food COD_N_CHIPS = new Food.Builder().nutrition(12).saturationMod(1.0f).build();
    public static final Food SALMON_TEMPURA = new Food.Builder().nutrition(12).saturationMod(1.0f).build();
}
