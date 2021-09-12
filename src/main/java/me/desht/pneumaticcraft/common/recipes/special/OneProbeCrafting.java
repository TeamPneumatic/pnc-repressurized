package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class OneProbeCrafting extends ShapelessRecipe {
    @ObjectHolder("theoneprobe:probe")
    public static final Item ONE_PROBE = null;

    private static final String ONE_PROBE_TAG = "theoneprobe";

    public OneProbeCrafting(ResourceLocation idIn) {
        super(idIn, "", makeOutputStack(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ModItems.PNEUMATIC_HELMET.get()), Ingredient.of(ONE_PROBE)));
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        if (ONE_PROBE == null) return false;

        boolean probeFound = false, helmetFound = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            Item item = inv.getItem(i).getItem();
            if (item == ModItems.PNEUMATIC_HELMET.get()) {
                if (helmetFound || isOneProbeEnabled(inv.getItem(i))) return false;
                helmetFound = true;
            } else if (item == ONE_PROBE) {
                if (probeFound) return false;
                probeFound = true;
            } else if (item != Items.AIR) {
                return false;
            }
        }
        return probeFound && helmetFound;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        ItemStack helmet = findHelmet(inv);
        if (helmet.isEmpty()) return ItemStack.EMPTY;
        ItemStack output = helmet.copy();
        setOneProbeEnabled(output);
        return output;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.ONE_PROBE_HELMET_CRAFTING.get();
    }

    private ItemStack findHelmet(CraftingInventory inv) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() == ModItems.PNEUMATIC_HELMET.get()) {
                return inv.getItem(i).copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack makeOutputStack() {
        ItemStack stack = new ItemStack(ModItems.PNEUMATIC_HELMET.get());
        setOneProbeEnabled(stack);
        return stack;
    }

    public static boolean isOneProbeEnabled(ItemStack helmetStack) {
        return helmetStack.hasTag() && helmetStack.getTag().getInt(ONE_PROBE_TAG) > 0;
    }

    private static void setOneProbeEnabled(ItemStack helmetStack) {
        helmetStack.getOrCreateTag().putInt(ONE_PROBE_TAG, 1);
    }
}
