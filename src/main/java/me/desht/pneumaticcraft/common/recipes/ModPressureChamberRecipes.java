package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.recipe.IPressureChamberRecipe;
import me.desht.pneumaticcraft.api.recipe.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(modid=Names.MOD_ID)
public class ModPressureChamberRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        Consumer<IPressureChamberRecipe> pc = evt.getPressureChamber();

        if (PNCConfig.Common.Recipes.coalToDiamondsRecipe) {

            pc.accept(new BasicPressureChamberRecipe(
                    RL("coal_to_diamonds"),
                    ImmutableList.of(StackedIngredient.fromTag(Tags.Items.STORAGE_BLOCKS_COAL, 8)),
                    4.0F,
                    new ItemStack(Items.DIAMOND)
            ));
        }

        pc.accept(new BasicPressureChamberRecipe(
                RL("compressed_iron_ingot"),
                ImmutableList.of(Ingredient.fromTag(Tags.Items.INGOTS_IRON)),
                2F,
                new ItemStack(ModItems.INGOT_IRON_COMPRESSED)
        ));

        pc.accept(new BasicPressureChamberRecipe(
                RL("compressed_iron_block"),
                ImmutableList.of(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON)),
                2F,
                new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK)
        ));

        pc.accept(new BasicPressureChamberRecipe(
                RL("turbine_blade"),
                ImmutableList.of(
                        StackedIngredient.fromTag(Tags.Items.DUSTS_REDSTONE, 2),
                        Ingredient.fromTag(Tags.Items.INGOTS_GOLD)
                ),
                1F,
                new ItemStack(ModItems.TURBINE_BLADE)
        ));

        ItemStack pcb = new ItemStack(ModItems.EMPTY_PCB);
        pcb.setDamage(pcb.getMaxDamage());
        pc.accept(new BasicPressureChamberRecipe(
                RL("empty_pcb"),
                ImmutableList.of(
                        Ingredient.fromItems(ModItems.PLASTIC),
                        Ingredient.fromItems(ModItems.INGOT_IRON_COMPRESSED)
                ),
                1.5F,
                pcb
        ));

        pc.accept(new BasicPressureChamberRecipe(
                RL("etching_acid_bucket"),
                ImmutableList.of(
                        Ingredient.fromItems(ModItems.PLASTIC_BUCKET),
                        Ingredient.fromStacks(new ItemStack(Items.ROTTEN_FLESH, 2)),
                        StackedIngredient.fromTag(Tags.Items.GUNPOWDER, 2),
                        Ingredient.fromStacks(new ItemStack(Items.SPIDER_EYE, 2))
                ),
                1.0F,
                new ItemStack(ModItems.ETCHING_ACID_BUCKET)
        ));

        pc.accept(new BasicPressureChamberRecipe(
                RL("transistor"),
                ImmutableList.of(
                        Ingredient.fromItems(ModItems.PLASTIC),
                        StackedIngredient.fromTag(Tags.Items.NUGGETS_GOLD, 3),
                        Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE)
                ),
                1.0F,
                new ItemStack(ModItems.TRANSISTOR)
        ));

        pc.accept(new BasicPressureChamberRecipe(
                RL("capacitor"),
                ImmutableList.of(
                        Ingredient.fromItems(ModItems.PLASTIC),
                        StackedIngredient.fromTag(Tags.Items.NUGGETS_GOLD, 2),
                        Ingredient.fromTag(Tags.Items.SLIMEBALLS)
                ),
                1.0F,
                new ItemStack(ModItems.CAPACITOR)
        ));

        pc.accept(new PressureChamberPressureEnchantHandler());

        pc.accept(new PressureChamberVacuumDisenchantHandler());
    }
}
