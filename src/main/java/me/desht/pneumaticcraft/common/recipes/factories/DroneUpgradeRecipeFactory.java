package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.item.ItemBasicDrone;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import javax.annotation.Nonnull;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class DroneUpgradeRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new DroneUpgradeRecipe(RL("drone_upgrade"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    public static class DroneUpgradeRecipe extends ShapelessOreRecipe {
        DroneUpgradeRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
            super(group, result, recipe);
        }

        @Nonnull
        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack basicDrone = ItemStack.EMPTY;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof ItemBasicDrone) {
                    basicDrone = stack.copy();
                    break;
                }
            }
            ItemStack drone = new ItemStack(Itemss.DRONE);
            NBTTagCompound droneTag = basicDrone.getTagCompound();
            if (droneTag == null) {
                droneTag = new NBTTagCompound();
                basicDrone.setTagCompound(droneTag);
            }
            drone.setTagCompound(droneTag);
            return drone;
        }

        @Override
        public boolean matches(InventoryCrafting inv, World world) {
            boolean hasDrone = false, hasPCB = false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.getItem() instanceof ItemBasicDrone) {
                    if (!hasDrone) hasDrone = true;
                    else return false;
                } else if (stack.getItem() == Itemss.PRINTED_CIRCUIT_BOARD) {
                    if (!hasPCB) hasPCB = true;
                    else return false;
                }
            }
            return hasDrone && hasPCB;
        }
    }
}
