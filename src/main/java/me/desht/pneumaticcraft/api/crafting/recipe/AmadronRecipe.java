package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import net.minecraft.util.ResourceLocation;

/**
 * NOTE: this only represents the trades loaded from datapack recipes.  Any trades discovered from villager trades,
 * or added by players, will not be included here.
 */
public abstract class AmadronRecipe extends PneumaticCraftRecipe {
    protected AmadronRecipe(ResourceLocation id) {
        super(id);
    }

    public abstract AmadronTradeResource getInput();

    public abstract AmadronTradeResource getOutput();

    public abstract String getVendor();

    public abstract boolean isStaticOffer();

    public abstract int getTradeLevel();
}
