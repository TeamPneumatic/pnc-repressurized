package me.desht.pneumaticcraft.api.harvesting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HoeHandler extends ForgeRegistryEntry<HoeHandler> implements Predicate<ItemStack> {
    private final Predicate<ItemStack> matchItem;
    private final BiConsumer<ItemStack, PlayerEntity> useDurability;

    public HoeHandler(Predicate<ItemStack> matchItem, BiConsumer<ItemStack, PlayerEntity> useDurability) {
        this.matchItem = matchItem;
        this.useDurability = useDurability;
    }

    @Override
    public boolean test(ItemStack stack) {
        return matchItem.test(stack);
    }

    public Consumer<PlayerEntity> getConsumer(ItemStack stack) {
        return player -> useDurability.accept(stack, player);
    }
}
