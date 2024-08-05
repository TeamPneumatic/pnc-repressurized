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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class WildcardedRLMatcher implements Predicate<ResourceLocation> {
    private final Set<String> namespaces = new ObjectOpenHashSet<>();
    private final Set<ResourceLocation> reslocs = new ObjectOpenHashSet<>();

    public static Lazy<WildcardedRLMatcher> lazyFromConfig(ModConfigSpec.ConfigValue<List<String>> configValue) {
        return Lazy.of(() -> new WildcardedRLMatcher(configValue.get()));
    }

    public WildcardedRLMatcher(Collection<String> toMatch) {
        for (String s : toMatch) {
            if (s.endsWith(":*")) {
                namespaces.add(s.split(":")[0]);
            } else {
                reslocs.add(ResourceLocation.parse(s));
            }
        }
    }

    public boolean isEmpty() {
        return reslocs.isEmpty() && namespaces.isEmpty();
    }

    @Override
    public boolean test(ResourceLocation resourceLocation) {
        return reslocs.contains(resourceLocation) || namespaces.contains(resourceLocation.getNamespace());
    }
}
