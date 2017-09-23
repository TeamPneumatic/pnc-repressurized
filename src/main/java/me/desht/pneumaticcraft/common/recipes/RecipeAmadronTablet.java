package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.common.item.ItemPlastic;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class RecipeAmadronTablet extends AbstractRecipe {
    private final ShapedOreRecipe recipe;
    private final CraftingHelper.ShapedPrimer primer;

    RecipeAmadronTablet() {
        super("amadron_tablet");

        this.primer = CraftingHelper.parseShaped("ppp", "pgp", "pcp",
                'p', new ItemStack(Itemss.PLASTIC, 1, ItemPlastic.GREY),
                'g', Itemss.GPS_TOOL,
                'c', new ItemStack(Itemss.AIR_CANISTER, 1, OreDictionary.WILDCARD_VALUE));
        this.recipe = new ShapedOreRecipe(RL("matcher_amadron_tablet"),
                new ItemStack(Itemss.AMADRON_TABLET, 1, Itemss.AMADRON_TABLET.getMaxDamage()), primer);
    }

    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        return recipe.matches(inventory, world);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack output = getRecipeOutput();
        output.setItemDamage(inventory.getStackInRowAndColumn(1, 2).getItemDamage());
        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= primer.width && height >= primer.height;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(Itemss.AMADRON_TABLET);
    }

}
