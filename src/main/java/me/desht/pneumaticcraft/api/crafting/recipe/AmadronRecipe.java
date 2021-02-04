package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import net.minecraft.util.ResourceLocation;

/**
 * An Amadron trade offer, loaded from datapack.  Note that any trades discovered from villager trades,
 * or added by players, do not come from datapacks and are not added to the vanilla recipe system (and thus will
 * not appear in JEI), but are still represented by instances of this class and displayed in the Amadron tablet.
 */
public abstract class AmadronRecipe extends PneumaticCraftRecipe {
    /**
     * Construct a new offer.  The ID must be unique; for offers loaded from datapack, it is derived from the mod
     * and JSON filename; for offers added by players, it is "pneumaticcraft:{playername}_{timestamp}"; for villager
     * trades, it is "{modname}:{profession}_{level}_{index}"
     * @param id the unique recipe ID
     */
    protected AmadronRecipe(ResourceLocation id) {
        super(id);
    }

    /**
     * Get the offer's input, i.e. what the Amadrone will collect from the player's Amadron inventory.
     * @return the input
     */
    public abstract AmadronTradeResource getInput();

    /**
     * Get the offer's output, i.e. what the player will receive in return from the Amadrone
     * @return the output
     */
    public abstract AmadronTradeResource getOutput();

    /**
     * Get the offer's vendor name, for display purposes.  This will be "Amadron" for static & periodic offers loaded
     * from datapack, "Villagers" for villager-discovered offers, and the player's name for player-added offers.
     *
     * @return the vendor name
     */
    public abstract String getVendor();

    /**
     * Is this a static offer, always displayed on the Amadron tablet?  Or periodic, shuffled in at random once per
     * Minecraft day (by default) ?
     * @return true if this is a static offer, false otherwise
     */
    public abstract boolean isStaticOffer();

    /**
     * The rarity for villager and periodic trades, in the range of 1 (common) to 5 (very rare)
     *
     * @return the rarity, or 0 if this is a static (always shown) offer
     */
    public abstract int getTradeLevel();

    /**
     * Get the number of trades Amadron currently has in stock for this offer. Note that all (default)
     * static offers have unlimited trades, as do all player-added offers.  Offers discovered from villager
     * trades do have limited stock (defined by the number of trades the villager would normally offer)
     *
     * @return the number of trades in stock, or any negative number for unlimited stock
     */
    public abstract int getStock();
}
