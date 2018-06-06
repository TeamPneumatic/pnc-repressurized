package me.desht.pneumaticcraft.common.recipes.factories;

import com.google.gson.JsonObject;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.recipes.CraftingRegistrator;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class OneProbeRecipeFactory implements IRecipeFactory {
    public static final String ONE_PROBE_TAG = "theoneprobe";

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapelessOreRecipe recipe = ShapelessOreRecipe.factory(context, json);
        return new OneProbeRecipeFactory.OneProbeRecipe(RL("one_probe"), recipe.getRecipeOutput(), recipe.getIngredients().toArray());
    }

    private class OneProbeRecipe extends ShapelessOreRecipe {
        OneProbeRecipe(ResourceLocation group, ItemStack result, Object... recipe) {
            super(group, result, recipe);
        }
        @Override
        public boolean matches(InventoryCrafting inv, World worldIn) {
            boolean probeFound = false, helmetFound = false;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                Item item = inv.getStackInSlot(i).getItem();
                if (item == Itemss.PNEUMATIC_HELMET) {
                    if (helmetFound) return false;
                    helmetFound = true;
                } else if (item == CraftingRegistrator.ONE_PROBE) {
                    if (probeFound) return false;
                    probeFound = true;
                } else if (item != Items.AIR) {
                    return false;
                }
            }
            return probeFound && helmetFound;
        }

        @Override
        public ItemStack getCraftingResult(InventoryCrafting inv) {
            ItemStack output = getRecipeOutput();
            ItemStack helmet = findHelmet(inv);
            NBTTagCompound tag = helmet.isEmpty() ? new NBTTagCompound() : helmet.hasTagCompound() ? helmet.getTagCompound().copy() : new NBTTagCompound();
            tag.setInteger(ONE_PROBE_TAG, 1);
            output.setTagCompound(tag);
            return output;
        }

        private ItemStack findHelmet(InventoryCrafting inv) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i).getItem() == Itemss.PNEUMATIC_HELMET) {
                    return inv.getStackInSlot(i).copy();
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
