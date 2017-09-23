package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class RecipeGun extends AbstractRecipe {
    private final Item output;
    private final ShapedOreRecipe recipe;
    private final CraftingHelper.ShapedPrimer primer;

    public RecipeGun(String dyeName, Item output) {
        super(output.getRegistryName().getResourcePath());
        this.output = output;
        this.primer = CraftingHelper.parseShaped("idi", "c  ", "ili",
                'd', dyeName, 'i', Itemss.INGOT_IRON_COMPRESSED, 'l', Blocks.LEVER,
                'c', new ItemStack(Itemss.AIR_CANISTER, 1, OreDictionary.WILDCARD_VALUE));
        this.recipe = new ShapedOreRecipe(RL("matcher_" + output.getRegistryName().getResourcePath()), getRecipeOutput(), primer);
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        return recipe.matches(inventory, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        if (!matches(inventory, null)) return null;
        ItemStack output = getRecipeOutput();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i).getItem() == Itemss.AIR_CANISTER) {
                output.setItemDamage(inventory.getStackInSlot(i).getItemDamage());
            }
        }
        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return height >= primer.height && width >= primer.width;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(output, 1, 0);
    }

}
