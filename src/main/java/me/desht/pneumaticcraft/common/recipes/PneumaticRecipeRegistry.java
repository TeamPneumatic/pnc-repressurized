package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.crafting.IModRecipeSerializer;
import me.desht.pneumaticcraft.api.crafting.IPneumaticRecipeRegistry;
import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public enum PneumaticRecipeRegistry implements IPneumaticRecipeRegistry {
    INSTANCE;

    public static PneumaticRecipeRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerDefaultStaticAmadronOffer(AmadronTradeResource input, AmadronTradeResource output) {
        AmadronOfferManager.getInstance().addStaticOffer(new AmadronOffer(input, output));
    }

    @Override
    public void registerDefaultPeriodicAmadronOffer(AmadronTradeResource input, AmadronTradeResource output) {
        AmadronOfferManager.getInstance().addPeriodicOffer(new AmadronOffer(input, output));
    }

    @Override
    public void registerSerializer(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> serializer) {
        ModCraftingHelper.register(recipeType, serializer);
    }
}
