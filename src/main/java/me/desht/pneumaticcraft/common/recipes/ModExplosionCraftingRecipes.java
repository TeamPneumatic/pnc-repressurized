package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.crafting.recipe.IExplosionCraftingRecipe;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.machine.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class ModExplosionCraftingRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        Consumer<IExplosionCraftingRecipe> exp = evt.getExplosionCrafting();

        exp.accept(new ExplosionCraftingRecipe(
                RL("compressed_iron_ingot"),
                Ingredient.fromTag(Tags.Items.INGOTS_IRON),
                20,
                new ItemStack(ModItems.INGOT_IRON_COMPRESSED.get())
        ));

        exp.accept(new ExplosionCraftingRecipe(
                RL("compressed_iron_block"),
                Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
                20,
                new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get())
        ));
    }
}
