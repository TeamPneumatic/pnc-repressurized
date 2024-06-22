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

import com.google.gson.JsonElement;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import vazkii.patchouli.api.IVariableSerializer;

public class FluidStackVariableSerializer implements IVariableSerializer<FluidStack> {
    @Override
    public FluidStack fromJson(JsonElement json) {
        if (json.isJsonNull()) {
            return FluidStack.EMPTY;
        } else if (json.isJsonPrimitive()) {
            return fluidStackFromString(json.getAsString());
        } else if (json.isJsonObject()) {
            return ModCraftingHelper.fluidStackFromJson(json.getAsJsonObject());
        } else {
            throw new IllegalArgumentException("Can't make an FluidStack from an array!");
        }
    }

    @Override
    public JsonElement toJson(FluidStack fluidStack) {
        return ModCraftingHelper.fluidStackToJson(fluidStack);
    }

    private static FluidStack fluidStackFromString(String s) {
        String id = s;
        int count = 1000;
        String[] split = s.split("#");
        if (split.length > 1) {
            id = split[0];
            count = Integer.parseInt(split[1]);
        }
        ResourceLocation fluidId = ResourceLocation.parse(id);
        if (!BuiltInRegistries.FLUID.containsKey(fluidId)) {
            throw new RuntimeException("Unknown fluid: " + id);
        }
        Fluid f = BuiltInRegistries.FLUID.get(fluidId);
        return new FluidStack(f, count);
    }
}
