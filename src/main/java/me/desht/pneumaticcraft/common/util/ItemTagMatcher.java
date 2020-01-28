package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Set;

public class ItemTagMatcher {
    private final Set<ResourceLocation> tags;

    private ItemTagMatcher(ItemStack stack) {
        this.tags = stack.getItem().getTags();
    }

    public boolean match(ItemStack stack) {
        return !Sets.intersection(tags, stack.getItem().getTags()).isEmpty();
    }

    public static boolean matchTags(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return new ItemTagMatcher(stack1).match(stack2);
    }
}
