package me.desht.pneumaticcraft.api.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class FluidIngredient extends Ingredient {
    private final FluidStack fluid;
    private ItemStack[] cachedStacks;

    public FluidIngredient(FluidStack fluid) {
        super(Stream.empty());
        this.fluid = fluid;
    }

    public FluidIngredient(Fluid fluid, int amount) {
        this(new FluidStack(fluid, amount));
    }

    public FluidStack getFluid() {
        return fluid;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return FluidUtil.getFluidContained(stack).map(fluidStack -> fluidStack.containsFluid(fluid)).orElse(false);
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        if (cachedStacks == null) {
            cachedStacks = new ItemStack[] { FluidUtil.getFilledBucket(fluid) };
        }
        return cachedStacks;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        json.addProperty("fluid", fluid.getFluid().getRegistryName().toString());
        json.addProperty("amount", fluid.getAmount());
        return json;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<FluidIngredient> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = RL("fluid");

        @Override
        public FluidIngredient parse(PacketBuffer buffer) {
            FluidStack stack = buffer.readFluidStack();
            if (stack.isEmpty()) throw new JsonSyntaxException("Failed to read fluidstack from buffer");
            return new FluidIngredient(stack);
        }

        @Override
        public FluidIngredient parse(JsonObject json) {
            ResourceLocation fluidName = new ResourceLocation(JSONUtils.getString(json, "fluid"));
            int amount = JSONUtils.getInt(json, "amount", 1000);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            if (fluid == null) throw new JsonSyntaxException("Unknown fluid: " + fluidName);
            return new FluidIngredient(fluid, amount);
        }

        @Override
        public void write(PacketBuffer buffer, FluidIngredient ingredient) {
            buffer.writeFluidStack(ingredient.getFluid());
        }
    }
}
