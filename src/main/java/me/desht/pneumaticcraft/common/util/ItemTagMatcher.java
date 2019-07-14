package me.desht.pneumaticcraft.common.util;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ItemTagMatcher {
    private final Set<ResourceLocation> tags;

    private ItemTagMatcher(ItemStack stack) {
        this.tags = TagOwnerTracker.getItemTags(stack);
    }

    public boolean match(ItemStack stack) {
        Set<ResourceLocation> tags1 = TagOwnerTracker.getItemTags(stack);
        tags1.retainAll(tags);
        return !tags1.isEmpty();
    }

    public static boolean matchTags(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return new ItemTagMatcher(stack1).match(stack2);
    }

    static class TagOwnerTracker implements ISelectiveResourceReloadListener {
        private static TagOwnerTracker INSTANCE;
        private static final Map<ResourceLocation, Set<ResourceLocation>> map = new HashMap<>();

        private TagOwnerTracker() { }

        public static TagOwnerTracker getInstance() {
            if (INSTANCE == null) {
                INSTANCE = new TagOwnerTracker();
                ServerLifecycleHooks.getCurrentServer().getResourceManager().addReloadListener(INSTANCE);
            }
            return INSTANCE;
        }

        <T extends IForgeRegistryEntry> Set<ResourceLocation> getOwningTags(TagCollection<T> collection, T t) {
            return map.computeIfAbsent(t.getRegistryName(), resourceLocation -> {
                Set<ResourceLocation> res = Sets.newHashSet();
                collection.getTagMap().forEach((resLoc, value) -> {
                    if (value.contains(t)) {
                        res.add(resLoc);
                    }
                });
                return res;
            });
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            map.clear();
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
            onResourceManagerReload(resourceManager);
        }

        static Set<ResourceLocation> getItemTags(ItemStack stack) {
            return getInstance().getOwningTags(ItemTags.getCollection(), stack.getItem());
        }
    }
}
