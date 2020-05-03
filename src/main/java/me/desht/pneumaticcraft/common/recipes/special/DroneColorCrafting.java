package me.desht.pneumaticcraft.common.recipes.special;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModRecipes;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.item.ItemDrone;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class DroneColorCrafting extends ShapelessRecipe {
    public DroneColorCrafting(ResourceLocation idIn) {
        super(idIn, "", new ItemStack(ModItems.DRONE.get()),
                NonNullList.from(Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.DYES), Ingredient.fromItems(ModItems.DRONE.get())));
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack drone = ItemStack.EMPTY;
        DyeColor dyeColor = null;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemDrone) {
                    drone = stack.copy();
                } else if (dyeColor == null) {
                    dyeColor = ((DyeItem) stack.getItem()).getDyeColor();
                }
            }
        }
        if (drone.isEmpty() || dyeColor == null) return ItemStack.EMPTY;

        CompoundNBT droneTag = drone.getTag();
        if (droneTag == null) {
            droneTag = new CompoundNBT();
            drone.setTag(droneTag);
        }
        droneTag.putInt(EntityDrone.NBT_DRONE_COLOR, dyeColor.getId());
        return drone;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipes.DRONE_COLOR_CRAFTING.get();
    }
}
