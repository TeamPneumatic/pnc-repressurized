package me.desht.pneumaticcraft.common.recipes.special;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider registryAccess) {
        ItemStack result = wrapped.assemble(inv, registryAccess);

        int index = getMainItem(inv);
        if (index == -1) {
            Log.warn("Just crafted a PNC Compressor upgrade recipe but couldn't find a compressor in the input!");
            return ItemStack.EMPTY;
        }
        ItemStack input = inv.getItem(index);

        CustomData data = input.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && !data.isEmpty()) {
            result.set(DataComponents.BLOCK_ENTITY_DATA, data);
        }
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
        public static final MapCodec<CompressorUpgradeCrafting> CODEC
                = ShapedRecipe.Serializer.CODEC.xmap(CompressorUpgradeCrafting::new, CompressorUpgradeCrafting::wrapped);
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressorUpgradeCrafting> STREAM_CODEC
                = ShapedRecipe.Serializer.STREAM_CODEC.map(CompressorUpgradeCrafting::new, CompressorUpgradeCrafting::wrapped);

        @Override
        public MapCodec<CompressorUpgradeCrafting> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompressorUpgradeCrafting> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
