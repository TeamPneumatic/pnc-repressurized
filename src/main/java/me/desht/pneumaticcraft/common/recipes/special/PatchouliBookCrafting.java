package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;

public class PatchouliBookCrafting extends ShapelessRecipe {
    @SuppressWarnings("FieldMayBeFinal")
    @ObjectHolder("patchouli:guide_book")
    public static Item GUIDE_BOOK = null;

    private static final String NBT_KEY = "patchouli:book";
    private static final String NBT_VAL = "pneumaticcraft:book";

    public PatchouliBookCrafting(ResourceLocation idIn) {
        super(idIn, "", makeGuideBook(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Items.BOOK), Ingredient.of(ModItems.COMPRESSED_IRON_INGOT.get()))
        );
    }

    private static ItemStack makeGuideBook() {
        if (GUIDE_BOOK == null) return ItemStack.EMPTY;
        ItemStack book = new ItemStack(GUIDE_BOOK);
        CompoundNBT tag = book.getOrCreateTag();
        tag.putString(NBT_KEY, NBT_VAL);
        return book;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.PATCHOULI_BOOK_CRAFTING.get();
    }
}
