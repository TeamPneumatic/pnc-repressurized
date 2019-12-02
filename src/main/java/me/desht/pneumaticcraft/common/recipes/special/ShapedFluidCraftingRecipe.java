package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.api.item.IItemRegistry.EnumUpgrade;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public abstract class ShapedFluidCraftingRecipe extends ShapedRecipe {
    ShapedFluidCraftingRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        int fluidSlot = findFluidContainer(inv);
        if (fluidSlot < 0) return false;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i != fluidSlot && !getIngredients().get(i).test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        int fluidSlot = findFluidContainer(inv);
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i == fluidSlot) {
                ItemStack drained = FluidUtil.getFluidHandler(stack).map(handler -> {
                    handler.drain(getRequiredFluid(), FluidAction.EXECUTE);
                    return handler.getContainer();
                }).orElse(ItemStack.EMPTY);
                ret.set(i, drained);
            } else {
                ret.set(i, ForgeHooks.getContainerItem(stack));
            }
        }
        return ret;
    }

    private int findFluidContainer(CraftingInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getCount() == 1 && !stack.getContainerItem().isEmpty()) {
                boolean foundFluid = FluidUtil.getFluidHandler(stack)
                        .map(handler -> {
                            FluidStack fluidStack = handler.drain(getRequiredFluid(), FluidAction.SIMULATE);
                            return fluidStack.getAmount() == getRequiredFluid().getAmount();
                        })
                        .orElse(false);
                if (foundFluid) return i;
            }
        }
        return -1;
    }

    protected abstract FluidStack getRequiredFluid();

    @Override
    public boolean isDynamic() {
        return true;
    }

    public static class SpeedUpgradeCraftingRecipe extends ShapedFluidCraftingRecipe {
        private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.from(
                Ingredient.fromItems(Items.LAPIS_LAZULI), Ingredient.fromItems(Items.SUGAR), Ingredient.fromItems(Items.LAPIS_LAZULI),
                Ingredient.fromItems(Items.SUGAR), null, Ingredient.fromItems(Items.SUGAR),
                Ingredient.fromItems(Items.LAPIS_LAZULI), Ingredient.fromItems(Items.SUGAR), Ingredient.fromItems(Items.LAPIS_LAZULI)
        );

        public SpeedUpgradeCraftingRecipe(ResourceLocation id) {
            super(id, "", 3, 3, INGREDIENTS, EnumUpgrade.SPEED.getItemStack());
        }

        @Override
        protected FluidStack getRequiredFluid() {
            return new FluidStack(ModFluids.LUBRICANT, 1000);
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return ModRecipes.SPEED_UPGRADE_CRAFTING;
        }
    }
}
