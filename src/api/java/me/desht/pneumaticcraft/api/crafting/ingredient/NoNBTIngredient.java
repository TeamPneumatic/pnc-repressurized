package me.desht.pneumaticcraft.api.crafting.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Like a vanilla ingredient, but requires the item must have no NBT whatsoever
 */
public class NoNBTIngredient extends Ingredient {

    private final ItemStack stack;

    public NoNBTIngredient(ItemStack stack) {
        super(Stream.of(new SingleItemList(stack)));
        this.stack = stack;
    }

    public NoNBTIngredient(IItemProvider item) {
        this(new ItemStack(item));
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && stack.getItem() == this.stack.getItem() && !stack.hasTag();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        json.addProperty("item", this.stack.getItem().getRegistryName().toString());
        json.addProperty("count", this.stack.getCount());
        return json;
    }

    @Nonnull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<NoNBTIngredient> {
        public static final IIngredientSerializer<NoNBTIngredient> INSTANCE = new NoNBTIngredient.Serializer();
        public static final ResourceLocation ID = new ResourceLocation("pneumaticcraft:no_nbt");

        @Override
        public NoNBTIngredient parse(PacketBuffer buffer) {
            return new NoNBTIngredient(buffer.readItem());
        }

        @Override
        public NoNBTIngredient parse(JsonObject json) {
            return new NoNBTIngredient(ShapedRecipe.itemFromJson(json));
        }

        @Override
        public void write(PacketBuffer buffer, NoNBTIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
        }
    }
}
