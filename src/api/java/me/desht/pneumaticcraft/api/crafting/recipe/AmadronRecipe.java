/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting.recipe;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.api.misc.IPlayerFilter;
import me.desht.pneumaticcraft.api.misc.IPlayerMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * An Amadron trade offer, loaded from datapack.  Note that any trades discovered from villager trades,
 * or added by players, do not come from datapacks and are not added to the vanilla recipe system (and thus will
 * not appear in JEI), but are still represented by instances of this class and displayed in the Amadron tablet.
 */
public abstract class AmadronRecipe extends PneumaticCraftRecipe {
    /**
     * Get the offer's input, i.e. what the Amadrone will collect from the player's Amadron inventory.
     * @return the input
     */
    @Nonnull
    public abstract AmadronTradeResource getInput();

    /**
     * Get the offer's output, i.e. what the player will receive in return from the Amadrone
     * @return the output
     */
    @Nonnull
    public abstract AmadronTradeResource getOutput();

    /**
     * Get the offer's vendor name, for display purposes.  The default is "Amadron" for static and periodic offers loaded
     * from datapack, "Villagers" for villager-discovered offers, and the player's display name (at the time of offer
     * creation) for player-added offers.
     *
     * @return the vendor name
     */
    public abstract Component getVendorName();

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

    /**
     * Update the number of trades Amadron currently has in stock for this offer. It is the responsibility of the
     * implementation to ensure the stock level does not go below 0, or above the max stock level as returned by
     * {@link #getMaxStock()} (provided that the max stock level is &gt; 0).
     *
     * @param stock the new stock level
     */
    public abstract void setStock(int stock);

    /**
     * Get the maximum (initial) stock level for this offer.
     *
     * @return the max stock level; any quantity &le; 0 indicates no maximum in force
     */
    public abstract int getMaxStock();

    /**
     * Can this offer be removed by the given player?
     *
     * @param player the player
     * @return true if this player can remove this offer from the system, false otherwise
     */
    public boolean isRemovableBy(Player player) {
        return false;
    }

    public abstract boolean isVillagerTrade();

    public abstract IPlayerFilter getWhitelist();

    public abstract IPlayerFilter getBlacklist();

    /**
     * Is this offer available to the given player?  By default, all offers are available, but offers can be
     * whitelisted/blacklisted in data packs with the "whitelist" and "blacklist" fields in the Amadron recipe JSON.
     * This could be used, for example, to only allow selling snow in a desert biome, or only purchasing ender pearls
     * in the End.  Default filters are "dimensions" and "biome_categories", but others can be added via
     * {@link me.desht.pneumaticcraft.api.misc.IMiscHelpers#registerPlayerMatcher(IPlayerMatcher.MatcherType)}.
     *
     * @param player the player to check
     * @return true if the offer is available to the player at the time of use, false otherwise
     */
    public abstract boolean isUsableByPlayer(Player player);

    /**
     * Does this offer match the given query string? The input resource, output resource and vendor names are all
     * tested (case-insensitive) for the query.
     *
     * @param query the query string
     * @return true if the recipe matches, false otherwise
     */
    public final boolean passesQuery(String query) {
        String queryLow = query.toLowerCase();
        if (queryLow.startsWith("@")) {
            // search by mod id
            String mod = queryLow.substring(1);
            return getInput().getId().getNamespace().toLowerCase().contains(mod)
                    || getOutput().getId().getNamespace().toLowerCase().contains(mod);
        }
        return getInput().getName().toLowerCase().contains(queryLow)
                || getOutput().getName().toLowerCase().contains(queryLow)
                || getVendorName().getString().toLowerCase().contains(queryLow);
    }

    /**
     * Add some information about where this offer is available, in the case of offers with limited availablity.
     * @param curTip tooltip to add information to
     */
    public void addAvailabilityData(Player player, List<Component> curTip) {
    }

    public boolean isLocationLimited() {
        return false;
    }
}
