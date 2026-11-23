package me.desht.pneumaticcraft.common.recipes.special;

import com.google.common.base.Suppliers;
import me.desht.pneumaticcraft.common.registry.ModRecipeSerializers;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

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
        return BuiltInRegistries.ITEM.getValue(RL(id));
    }

    @Override
    protected RecipeSerializer<CompressorUpgradeCrafting> wrappedSerializer() {
        return ModRecipeSerializers.COMPRESSOR_UPGRADE_CRAFTING.get();
    }

    @NotNull
    @Override
    public ItemStack assemble(CraftingInput inv) {
        ItemStack result = super.assemble(inv);

        int index = getMainItem(inv);
        if (index == -1) {
            Log.warning("Just crafted a PNC Compressor upgrade recipe but couldn't find a compressor in the input!");
            return ItemStack.EMPTY;
        }
        ItemStack input = inv.getItem(index);

        TypedEntityData<BlockEntityType<?>> data = input.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            result.set(DataComponents.BLOCK_ENTITY_DATA, data);
        }
        return result;
    }

    private int getMainItem(CraftingInput container) {
        int i;
        boolean matchFound = false;

        for (i = 0; i < container.size(); i++) {
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

    public static RecipeSerializer<CompressorUpgradeCrafting> serializer() {
        return serializer(CompressorUpgradeCrafting::new);
    }
}
