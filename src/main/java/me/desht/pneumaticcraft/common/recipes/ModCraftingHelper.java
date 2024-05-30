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

package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

public class ModCraftingHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static FluidStack fluidStackFromJson(JsonObject json) {
        String fluidName = GsonHelper.getAsString(json, "fluid");
        if (fluidName.equals("minecraft:empty")) return FluidStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(fluidName));
        if (fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("unknown fluid: " + fluidName);
        }
        int amount = GsonHelper.getAsInt(json, "amount", 1000);
        FluidStack fluidStack = new FluidStack(fluid, amount);
        if (json.has("nbt")) {
            JsonElement element = json.get("nbt");
            try {
                if (element.isJsonObject()) {
                    fluidStack.setTag(TagParser.parseTag(GSON.toJson(element)));
                } else {
                    fluidStack.setTag(TagParser.parseTag(GsonHelper.convertToString(element, "nbt")));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e);
            }
        }
        return fluidStack;
    }

    public static JsonObject fluidStackToJson(FluidStack f) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", PneumaticCraftUtils.getRegistryName(f.getFluid()).orElseThrow().toString());
        json.addProperty("amount", f.getAmount());
        if (f.hasTag()) {
            json.addProperty("nbt", f.getTag().toString());
        }
        return json;
    }

    public static List<ItemStack> findItems(CraftingContainer inv, List<Predicate<ItemStack>> predicates) {
        List<ItemStack> res = new ArrayList<>();
        BitSet matchedSlots = new BitSet(inv.getContainerSize());

        for (var pred : predicates) {
            boolean found = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (!matchedSlots.get(i)) {
                    ItemStack stack = inv.getItem(i);
                    if (pred.test(stack)) {
                        res.add(stack);
                        matchedSlots.set(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return List.of();
        }

        // check any unmatched slots for extraneous items
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (!matchedSlots.get(i) && !inv.getItem(i).isEmpty()) return List.of();
        }

        return res;
    }

    public static boolean allPresent(CraftingContainer inv, List<Predicate<ItemStack>> predicates) {
        return findItems(inv, predicates).size() == predicates.size();
    }
}
