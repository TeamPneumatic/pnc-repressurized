package me.desht.pneumaticcraft.common.thirdparty.patchouli;

import com.google.gson.JsonElement;
import me.desht.pneumaticcraft.common.recipes.ModCraftingHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
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
        ResourceLocation fluidId = new ResourceLocation(id);
        if (!ForgeRegistries.FLUIDS.containsKey(fluidId)) {
            throw new RuntimeException("Unknown fluid: " + id);
        }
        Fluid f = ForgeRegistries.FLUIDS.getValue(fluidId);
        return new FluidStack(f, count);
    }
}
