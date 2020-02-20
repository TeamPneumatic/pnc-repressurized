package me.desht.pneumaticcraft.common.util;

import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;

import java.util.Random;
import java.util.function.Function;

public class RandomTradeBuilder
{
    private Function<Random, ItemStack> price;
    private Function<Random, ItemStack> price2;
    private Function<Random, ItemStack> forSale;

    private final int maxTrades;
    private final int xp;
    private final float priceMult;

    public RandomTradeBuilder(int maxTrades, int xp, float priceMult)
    {
        this.price = null;
        this.price2 = (random) -> ItemStack.EMPTY;
        this.forSale = null;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public RandomTradeBuilder setPrice(Function<Random, ItemStack> price)
    {
        this.price = price;
        return this;
    }

    public RandomTradeBuilder setPrice(Item item, int min, int max)
    {
        return this.setPrice(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setPrice2(Function<Random, ItemStack> price2)
    {
        this.price2 = price2;
        return this;
    }

    public RandomTradeBuilder setPrice2(Item item, int min, int max)
    {
        return this.setPrice2(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setForSale(Function<Random, ItemStack> forSale)
    {
        this.forSale = forSale;
        return this;
    }

    public RandomTradeBuilder setForSale(Item item, int min, int max)
    {
        return this.setForSale(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setEmeraldPrice(int emeralds)
    {
        return this.setPrice((random) -> new ItemStack(Items.EMERALD, emeralds));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item, int amt)
    {
        this.setEmeraldPrice(emeralds);
        return this.setForSale((random) -> new ItemStack(item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item)
    {
        return this.setEmeraldPriceFor(emeralds, item, 1);
    }

    public RandomTradeBuilder setEmeraldPrice(int min, int max)
    {
        return this.setPrice(Items.EMERALD, min, max);
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item, int amt)
    {
        this.setEmeraldPrice(min, max);
        return this.setForSale((random) -> new ItemStack(item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item)
    {
        return this.setEmeraldPriceFor(min, max, item, 1);
    }

    public boolean canBuild()
    {
        return this.price != null && this.forSale != null;
    }

    public VillagerTrades.ITrade build()
    {
        return (entity, random) -> !this.canBuild() ? null : new MerchantOffer(this.price.apply(random), this.price2.apply(random), this.forSale.apply(random), this.maxTrades, this.xp, this.priceMult);
    }

    public static Function<Random, ItemStack> createFunction(Item item, int min, int max)
    {
        return (random) -> new ItemStack(item, random.nextInt(max) + min);
    }
}
