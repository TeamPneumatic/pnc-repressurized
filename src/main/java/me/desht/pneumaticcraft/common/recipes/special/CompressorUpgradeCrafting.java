package me.desht.pneumaticcraft.common.recipes.special;

import cofh.lib.util.constants.NBTTags;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.Log;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CompressorUpgradeCrafting extends WrappedShapedRecipe {
    private static final Supplier<List<Item>> COMPRESSORS = Suppliers.memoize(() -> List.of(
            getItem("advanced_air_compressor"),
            getItem("advanced_liquid_compressor"),
            getItem("air_compressor"),
            getItem("liquid_compressor")
    ));

    public CompressorUpgradeCrafting(ShapedRecipe wrapped) {
        super(wrapped);
    }

    private static Item getItem(String id) {
        return BuiltInRegistries.ITEM.get(RL(id));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.COMPRESSOR_UPGRADE_CRAFTING.get();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack result = wrapped.assemble(inv, registryAccess);

        int index = getMainItem(inv);
        if (index == -1) {
            Log.warn("Just crafted a PNC Compressor upgrade recipe but couldn't find a compressor in the input!");
            return ItemStack.EMPTY;
        }
        ItemStack input = inv.getItem(index);

        CompoundTag tag = input.getTag();
        if (tag == null) return result;

        Tag blockEntityTag = tag.get(NBTTags.TAG_BLOCK_ENTITY);
        if (blockEntityTag == null) return result;

        result.getOrCreateTag().put(NBTTags.TAG_BLOCK_ENTITY, blockEntityTag);
        return result;
    }

    private int getMainItem(CraftingContainer container) {
        int i;
        boolean matchFound = false;

        for (i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (COMPRESSORS.get().contains(item.getItem())) {
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            return -1;
        }
        return i;
    }

    public static class Serializer implements RecipeSerializer<CompressorUpgradeCrafting> {
        public static final Codec<CompressorUpgradeCrafting> CODEC = ShapedRecipe.Serializer.CODEC.xmap(
                CompressorUpgradeCrafting::new, CompressorUpgradeCrafting::getWrapped
        );

        @Override
        public Codec<CompressorUpgradeCrafting> codec() {
            return CODEC;
        }

        @Override
        public CompressorUpgradeCrafting fromNetwork(FriendlyByteBuf buf) {
            return new CompressorUpgradeCrafting(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buf));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CompressorUpgradeCrafting recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe.wrapped);
        }
    }
}
