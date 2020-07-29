package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;

public class DroneColorCrafting extends ShapelessRecipe {
    // you'd think using Ingredient.fromTag(Tags.Items.Dyes) would work, but nope
    private static final Item[] DYES = new Item[DyeColor.values().length];
    static {
        Arrays.setAll(DYES, i -> DyeItem.getItem(DyeColor.values()[i]));
    }

    public DroneColorCrafting(ResourceLocation idIn) {
        super(idIn, "", new ItemStack(ModItems.DRONE.get()), NonNullList.from(Ingredient.EMPTY,
                    Ingredient.fromItems(DYES), Ingredient.fromItems(ModItems.DRONE.get()))
        );
    }

    private Pair<ItemStack, DyeColor> findItems(CraftingInventory inv) {
        ItemStack drone = ItemStack.EMPTY;
        DyeColor dye = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == ModItems.DRONE.get()) {
                if (!drone.isEmpty()) return null;
                drone = stack.copy();
            } else if (dye == null) {
                DyeColor color = DyeColor.getColor(stack);
                if (color != null) {
                    dye = color;
                }
            } else if (!stack.isEmpty()) {
                return null;
            }
            if (!drone.isEmpty() && dye != null) break;
        }
        return drone.isEmpty() || dye == null ? null : Pair.of(drone, dye);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return findItems(inv) != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        Pair<ItemStack, DyeColor> data = findItems(inv);
        if (data == null) return ItemStack.EMPTY;
        ItemStack drone = data.getLeft();
        DyeColor dyeColor = data.getRight();
        if (drone.isEmpty() || dyeColor == null) return ItemStack.EMPTY;

        CompoundNBT droneTag = drone.getOrCreateTag();
        droneTag.putInt(EntityDrone.NBT_DRONE_COLOR, dyeColor.getId());
        return drone;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRONE_COLOR_CRAFTING.get();
    }
}
