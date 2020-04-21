package me.desht.pneumaticcraft.datagen.recipe;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class HeatFrameCoolingRecipeBuilder extends PneumaticCraftRecipeBuilder<HeatFrameCoolingRecipeBuilder> {
    private final Ingredient input;
    private final int temperature;
    private final ItemStack output;
    private final float bonusMultiplier;
    private final float bonusLimit;

    protected HeatFrameCoolingRecipeBuilder(Ingredient input, int temperature, ItemStack output) {
        this(input, temperature, output, 0f, 0f);
    }

    public HeatFrameCoolingRecipeBuilder(Ingredient input, int temperature, ItemStack output, float bonusMultiplier, float bonusLimit) {
        super(RL(PneumaticCraftRecipeTypes.HEAT_FRAME_COOLING));

        this.input = input;
        this.temperature = temperature;
        this.output = output;
        this.bonusMultiplier = bonusMultiplier;
        this.bonusLimit = bonusLimit;
    }

    @Override
    protected RecipeResult getResult(ResourceLocation id) {
        return new HeatFrameCoolingRecipeResult(id);
    }

    public class HeatFrameCoolingRecipeResult extends RecipeResult {
        HeatFrameCoolingRecipeResult(ResourceLocation id) {
            super(id);
        }

        @Override
        public void serialize(JsonObject json) {
            json.add("input", input.serialize());
            json.addProperty("max_temp", temperature);
            json.add("result", SerializerHelper.serializeOneItemStack(output));
            if (bonusMultiplier > 0f || bonusLimit > 0f) {
                JsonObject bonus = new JsonObject();
                bonus.addProperty("multiplier", bonusMultiplier);
                bonus.addProperty("limit", bonusLimit);
                json.add("bonus_output", bonus);
            }
        }
    }
}
