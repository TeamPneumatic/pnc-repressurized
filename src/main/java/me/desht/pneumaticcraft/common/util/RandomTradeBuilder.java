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

package me.desht.pneumaticcraft.common.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class RandomTradeBuilder {
    @Nullable
    private Function<RandomSource, ItemCost> price;
    @Nullable
    private Function<RandomSource, ItemCost> price2;
    private Function<RandomSource, ItemStack> forSale;

    private final int maxTrades;
    private final int xp;
    private final float priceMult;

    public RandomTradeBuilder(int maxTrades, int xp, float priceMult) {
        price = null;
        price2 = null;
        forSale = null;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public RandomTradeBuilder setPrice(Function<RandomSource, ItemCost> price) {
        this.price = price;
        return this;
    }

    public RandomTradeBuilder setPrice(Item item, int min, int max) {
        return setPrice(RandomTradeBuilder.createCost(item, min, max));
    }

    public RandomTradeBuilder setPrice2(Function<RandomSource, ItemCost> price2) {
        this.price2 = price2;
        return this;
    }

    public RandomTradeBuilder setPrice2(Item item, int min, int max) {
        return setPrice2(RandomTradeBuilder.createCost(item, min, max));
    }

    public RandomTradeBuilder setForSale(Function<RandomSource, ItemStack> forSale) {
        this.forSale = forSale;
        return this;
    }

    public RandomTradeBuilder setForSale(Item item, int min, int max) {
        return setForSale(RandomTradeBuilder.createResult(item, min, max));
    }

    public RandomTradeBuilder setEmeraldPrice(int emeralds) {
        return setPrice(random -> new ItemCost(Items.EMERALD, emeralds));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item, int amt) {
        setEmeraldPrice(emeralds);
        return setForSale(random -> new ItemStack(item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item) {
        return setEmeraldPriceFor(emeralds, item, 1);
    }

    public RandomTradeBuilder setEmeraldPrice(int min, int max) {
        return setPrice(Items.EMERALD, min, max);
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item, int amt) {
        setEmeraldPrice(min, max);
        return setForSale(random -> new ItemStack(item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item) {
        return setEmeraldPriceFor(min, max, item, 1);
    }

    public boolean canBuild() {
        return price != null && forSale != null;
    }

    public VillagerTrades.ItemListing build() {
        return (entity, random) -> canBuild() ?
                new MerchantOffer(
                        Objects.requireNonNull(price).apply(random),
                        Optional.ofNullable(price2).map(p -> p.apply(random)),
                        forSale.apply(random),
                        maxTrades,
                        xp, 
                        priceMult
                ) : null;
    }

    public static Function<RandomSource, ItemCost> createCost(Item item, int min, int max) {
        return random -> new ItemCost(item, random.nextInt(max - min) + min);
    }

    public static Function<RandomSource, ItemStack> createResult(Item item, int min, int max) {
        return random -> new ItemStack(item, random.nextInt(max - min) + min);
    }
}
