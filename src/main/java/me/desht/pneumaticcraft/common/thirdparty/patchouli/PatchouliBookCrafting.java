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

package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import com.google.common.base.Suppliers;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class PatchouliBookCrafting {
    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "pneumaticcraft:book";

    private static final Supplier<Item> guideBook
            = Suppliers.memoize(() -> BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModIds.PATCHOULI, "guide_book")));

    public static ItemStack makeGuideBook() {
//        return Util.make(new ItemStack(guideBook.get()), s -> s.getOrCreateTag().putString(NBT_KEY, NBT_VAL));
        // TODO patchouli will presumable add a data component for this?
        return new ItemStack(guideBook.get());
    }
}
