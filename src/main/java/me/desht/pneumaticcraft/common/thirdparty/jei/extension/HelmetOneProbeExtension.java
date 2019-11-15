package me.desht.pneumaticcraft.common.thirdparty.jei.extension;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class HelmetOneProbeExtension implements ICraftingCategoryExtension {
    private final List<List<ItemStack>> inputs;
    private final ItemStack output;
    private final ResourceLocation name;

    public HelmetOneProbeExtension(OneProbeCrafting recipe) {
        inputs = ImmutableList.of(
                ImmutableList.of(new ItemStack(ModItems.PNEUMATIC_HELMET)),
                ImmutableList.of(new ItemStack(OneProbeCrafting.ONE_PROBE))
        );
        output = new ItemStack(ModItems.PNEUMATIC_HELMET);
        OneProbeCrafting.setOneProbeEnabled(output);
        name = recipe.getId();
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }
}
