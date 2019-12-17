package me.desht.pneumaticcraft.common.thirdparty.jei.extension;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import net.minecraft.item.ItemStack;

public class HelmetOneProbeExtension extends AbstractShapelessExtension {
    public HelmetOneProbeExtension(OneProbeCrafting recipe) {
        super(recipe, new ItemStack(ModItems.PNEUMATIC_HELMET), ModItems.PNEUMATIC_HELMET, OneProbeCrafting.ONE_PROBE);

        OneProbeCrafting.setOneProbeEnabled(getOutput());
    }
}
