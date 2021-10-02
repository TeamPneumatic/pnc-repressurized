package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCraftingHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static FluidStack fluidStackFromJson(JsonObject json) {
        String fluidName = JSONUtils.getAsString(json, "fluid");
        if (fluidName.equals("minecraft:empty")) return FluidStack.EMPTY;
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("unknown fluid: " + fluidName);
        }
        int amount = JSONUtils.getAsInt(json, "amount", 1000);
        FluidStack fluidStack = new FluidStack(fluid, amount);
        if (json.has("nbt")) {
            JsonElement element = json.get("nbt");
            try {
                if (element.isJsonObject()) {
                    fluidStack.setTag(JsonToNBT.parseTag(GSON.toJson(element)));
                } else {
                    fluidStack.setTag(JsonToNBT.parseTag(JSONUtils.convertToString(element, "nbt")));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException(e);
            }
        }
        return fluidStack;
    }

    public static JsonObject fluidStackToJson(FluidStack f) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", f.getFluid().getRegistryName().toString());
        json.addProperty("amount", f.getAmount());
        if (f.hasTag()) {
            json.addProperty("nbt", f.getTag().toString());
        }
        return json;
    }
}
