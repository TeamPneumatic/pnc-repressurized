package me.desht.pneumaticcraft.common.item;

import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

/**
 * Implement on items which need to add multiple stacks to the PNC creative tab
 */
public interface CreativeTabStackProvider {
    Stream<ItemStack> getStacksForItem();
}
