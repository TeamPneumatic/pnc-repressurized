package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.recipes.SimpleRecipeSerializer;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SimpleRecipeBuilder extends CraftingRecipeBuilder {
   final RecipeSerializer<?> serializer;

   public SimpleRecipeBuilder(RecipeSerializer<?> pSerializer) {
      this.serializer = pSerializer;
   }

   public static SimpleRecipeBuilder simple(SimpleRecipeSerializer<? extends Recipe> pSerializer) {
      return new SimpleRecipeBuilder(pSerializer);
   }

   /**
    * Builds this recipe into an {@link FinishedRecipe}.
    */
   public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, final String pId) {
      pFinishedRecipeConsumer.accept(new CraftingResult(CraftingBookCategory.MISC) {
         public RecipeSerializer<?> getType() {
            return SimpleRecipeBuilder.this.serializer;
         }

         /**
          * Gets the ID for the recipe.
          */
         public ResourceLocation getId() {
            return new ResourceLocation(pId);
         }

         /**
          * Gets the JSON for the advancement that unlocks this recipe. Null if there is no advancement.
          */
         @Nullable
         public JsonObject serializeAdvancement() {
            return null;
         }

         /**
          * Gets the ID for the advancement associated with this recipe. Should not be null if {@link
          * #serializeAdvancement()} is non-null.
          */
         public ResourceLocation getAdvancementId() {
            return new ResourceLocation("");
         }
      });
   }
}