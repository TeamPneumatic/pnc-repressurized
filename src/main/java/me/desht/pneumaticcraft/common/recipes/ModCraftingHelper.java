package me.desht.pneumaticcraft.common.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.desht.pneumaticcraft.api.crafting.IModRecipeSerializer;
import me.desht.pneumaticcraft.api.crafting.recipe.IModRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModCraftingHelper {
    private static final Supplier<IModRecipeSerializer<?>> MISSING = () -> null;
    private static final Map<ResourceLocation, Supplier<IModRecipeSerializer<? extends IModRecipe>>> factories = new HashMap<>();

    static void register(ResourceLocation recipeType, Supplier<IModRecipeSerializer<? extends IModRecipe>> deserializer) {
        factories.put(recipeType, deserializer);
    }

    public static <T extends IModRecipe> IModRecipeSerializer<T> getSerializer(ResourceLocation type) {
        //noinspection unchecked
        return (IModRecipeSerializer<T>) factories.getOrDefault(type, MISSING).get();
    }

    public static <T extends IModRecipe> IModRecipeSerializer<T> getSerializer(T recipe) {
        //noinspection unchecked
        return (IModRecipeSerializer<T>) factories.getOrDefault(recipe.getRecipeType(), MISSING).get();
    }

    public static void writeRecipe(IModRecipe recipe, PacketBuffer buf) {
        getSerializer(recipe).write(buf, recipe);
    }

    public static <T extends IModRecipe> T readRecipe(PacketBuffer buf) {
        //noinspection unchecked
        return (T) getSerializer(buf.readResourceLocation()).read(buf.readResourceLocation(), buf);
    }

    public static FluidStack fluidStackFromJSON(JsonObject json) {
        String fluidName = JSONUtils.getString(json, "fluid");
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
        if (fluid == null || fluid == Fluids.EMPTY) throw new JsonSyntaxException("unknown fluid: " + fluidName);
        int amount = JSONUtils.getInt(json, "amount", 1000);
        return new FluidStack(fluid, amount);
    }
}
