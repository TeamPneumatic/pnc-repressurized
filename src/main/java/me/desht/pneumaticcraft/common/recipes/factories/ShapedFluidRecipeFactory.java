package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

// With thanks to CoFH for showing me how this works!
public class ShapedFluidRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getRecipeWidth();
		primer.height = recipe.getRecipeHeight();
		primer.mirrored = JSONUtils.getBoolean(json, "mirrored", true);
		primer.input = recipe.getIngredients();

        return new ShapedFluidRecipe(RL("shaped_fluid"), recipe.getRecipeOutput(), primer);
    }

    public static class ShapedFluidRecipe extends ShapedOreRecipe {
        public ShapedFluidRecipe(ResourceLocation group, ItemStack result, ShapedPrimer recipe) {
            super(group, result, recipe);
        }

        @Override
        public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {

            NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

            for (int i = 0; i < ret.size(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                IFluidHandlerItem handler = stack.getCount() > 1 ?
                        FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(stack, 1)) :
                        FluidUtil.getFluidHandler(stack);

                if (handler == null) {
                    ret.set(i, ForgeHooks.getContainerItem(stack));
                } else {
                    handler.drain(Fluid.BUCKET_VOLUME, true);
                    ret.set(i, handler.getContainer().copy());
                }
            }
            return ret;
        }

        @Override
        public boolean isDynamic() {
            return true;
        }
    }
}
