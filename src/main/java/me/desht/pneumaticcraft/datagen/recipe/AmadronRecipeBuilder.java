/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.datagen.recipe;

import me.desht.pneumaticcraft.api.crafting.AmadronTradeResource;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.util.playerfilter.PlayerFilter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

public class AmadronRecipeBuilder extends AbstractPNCRecipeBuilder {
    private final AmadronTradeResource inputResource;
    private final AmadronTradeResource outputResource;
    private final boolean isStatic;
    private final boolean isVillagerTrade;
    private final int level;
    private final int maxStock;
    private final PlayerFilter whitelist;
    private final PlayerFilter blacklist;

    public AmadronRecipeBuilder(AmadronTradeResource input, AmadronTradeResource output, boolean isStatic, boolean isVillagerTrade,  int level,
                                int maxStock, PlayerFilter whitelist, PlayerFilter blacklist) {
        this.inputResource = input;
        this.outputResource = output;
        this.isStatic = isStatic;
        this.isVillagerTrade = isVillagerTrade;
        this.level = level;
        this.maxStock = maxStock;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public AmadronRecipeBuilder(AmadronTradeResource input, AmadronTradeResource output, boolean isStatic, int level) {
        this(input, output, isStatic, false, level, -1, PlayerFilter.YES, PlayerFilter.NO);
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, new AmadronOffer(id, inputResource, outputResource, isStatic, isVillagerTrade, level, maxStock, maxStock, whitelist, blacklist), null);
    }
}
