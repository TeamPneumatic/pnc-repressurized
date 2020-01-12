package me.desht.pneumaticcraft.common.thirdparty.jei.extension;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import net.minecraft.item.ItemStack;

public class HelmetOneProbeExtension extends AbstractShapelessExtension {
    public HelmetOneProbeExtension(OneProbeCrafting recipe) {
        super(recipe, new ItemStack(ModItems.PNEUMATIC_HELMET.get()), ModItems.PNEUMATIC_HELMET.get(), OneProbeCrafting.ONE_PROBE);

        OneProbeCrafting.setOneProbeEnabled(getOutput());
    }
}
