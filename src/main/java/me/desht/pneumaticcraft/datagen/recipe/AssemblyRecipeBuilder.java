package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class AssemblyRecipeBuilder extends PneumaticCraftRecipeBuilder<AssemblyRecipeBuilder> {
    private final Ingredient input;
    @Nonnull
    private final ItemStack output;
    private final AssemblyRecipe.AssemblyProgramType program;

    public AssemblyRecipeBuilder(Ingredient input, @Nonnull ItemStack output, AssemblyRecipe.AssemblyProgramType program) {
        super(program.getRecipeType());

        this.input = input;
        this.output = output;
        this.program = program;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new AssemblyRecipeResult(id);
    }

    public class AssemblyRecipeResult extends RecipeResult {
        AssemblyRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("input", input.serialize());
            json.add("result", SerializerHelper.serializeOneItemStack(output));
            json.addProperty("program", program.toString().toLowerCase());
        }
    }
}
