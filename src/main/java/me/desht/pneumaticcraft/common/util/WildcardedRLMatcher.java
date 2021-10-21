package me.desht.pneumaticcraft.common.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public class WildcardedRLMatcher implements Predicate<ResourceLocation> {
    private final Set<String> namespaces = new ObjectOpenHashSet<>();
    private final Set<ResourceLocation> reslocs = new ObjectOpenHashSet<>();

    public WildcardedRLMatcher(Collection<String> toMatch) {
        for (String s : toMatch) {
            if (s.endsWith(":*")) {
                namespaces.add(s.split(":")[0]);
            } else {
                reslocs.add(new ResourceLocation(s));
            }
        }
    }

    @Override
    public boolean test(ResourceLocation resourceLocation) {
        return reslocs.contains(resourceLocation) || namespaces.contains(resourceLocation.getNamespace());
    }
}
