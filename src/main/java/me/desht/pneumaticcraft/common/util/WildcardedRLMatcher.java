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
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.ResourceLocationException;
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

    public static Lazy<WildcardedRLMatcher> lazyFromConfig(ModConfigSpec.ConfigValue<List<? extends String>> configValue) {
        return Lazy.of(() -> new WildcardedRLMatcher(configValue.get()));
    }

    public WildcardedRLMatcher(Collection<? extends String> toMatch) {
        for (String str : toMatch) {
            String[] parts = str.split(":", 2);
            if (parts.length == 1 || parts[1].isEmpty() || parts[1].equals("*")) {
                namespaces.add(parts[0]);
            } else {
                try {
                    reslocs.add(ResourceLocation.parse(str));
                } catch (ResourceLocationException e) {
                    Log.error("WildcardedRLMatcher: invalid resource location '{}'", str);
                }
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

    public static boolean isValidRL(Object input) {
        if (!(input instanceof String str)) {
            return false;
        }
        if (ResourceLocation.tryParse(str) != null) {
            return true;
        }
        String[] parts = str.split(":");
        return parts.length == 2 && ResourceLocation.isValidNamespace(parts[0]) && (parts[0].isEmpty() || parts[1].equals("*"));
    }
}
