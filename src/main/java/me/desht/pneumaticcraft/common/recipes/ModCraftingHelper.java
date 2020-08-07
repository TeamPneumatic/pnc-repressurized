package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCraftingHelper {
    public static FluidStack fluidStackFromJson(JsonObject json) {
        String fluidName = JSONUtils.getString(json, "fluid");
        if (fluidName.equals("minecraft:empty")) return FluidStack.EMPTY;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("unknown fluid: " + fluidName);
        int amount = JSONUtils.getInt(json, "amount", 1000);
        return new FluidStack(fluid, amount);
    }

    public static JsonObject fluidStackToJson(FluidStack f) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", f.getFluid().getRegistryName().toString());
        json.addProperty("amount", f.getAmount());
        return json;
    }
}
