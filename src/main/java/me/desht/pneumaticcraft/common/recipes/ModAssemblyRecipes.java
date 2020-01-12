package me.desht.pneumaticcraft.common.recipes;


import me.desht.pneumaticcraft.api.crafting.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.crafting.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.machine.AssemblyRecipe;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class ModAssemblyRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        Consumer<IAssemblyRecipe> assembly = evt.getAssembly();

        assembly.accept(new AssemblyRecipe(
                RL("unassembled_pcb"),
                Ingredient.fromItems(ModItems.EMPTY_PCB.get()),
                new ItemStack(ModItems.UNASSEMBLED_PCB.get()),
                AssemblyProgramType.LASER
        ));

        assembly.accept(new AssemblyRecipe(
                RL("advanced_pressure_tube"),
                StackedIngredient.fromItems(20, ModBlocks.PRESSURE_CHAMBER_VALVE.get()),
                new ItemStack(ModBlocks.ADVANCED_PRESSURE_TUBE.get(), 8),
                AssemblyProgramType.LASER
        ));

        assembly.accept(new AssemblyRecipe(
                RL("aphorism_tile"),
                Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_QUARTZ),
                new ItemStack(ModBlocks.APHORISM_TILE.get(), 4),
                AssemblyProgramType.LASER
        ));

        assembly.accept(new AssemblyRecipe(
                RL("pressure_chamber_valve"),
                Ingredient.fromItems(ModBlocks.COMPRESSED_IRON_BLOCK.get()),
                new ItemStack(ModBlocks.PRESSURE_CHAMBER_VALVE.get(), 20),
                AssemblyProgramType.DRILL
        ));

        assembly.accept(new AssemblyRecipe(
                RL("red_dye"),
                Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
                new ItemStack(Items.RED_DYE),
                AssemblyProgramType.DRILL
        ));
    }
}
