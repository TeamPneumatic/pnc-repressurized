package me.desht.pneumaticcraft.common.recipes;


import me.desht.pneumaticcraft.api.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.recipe.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(modid= Names.MOD_ID)
public class ModHeatFrameCoolingRecipes {
    @SubscribeEvent
    public static void register(RegisterMachineRecipesEvent evt) {
        Consumer<IHeatFrameCoolingRecipe> hfc = evt.getHeatFrameCooling();

        hfc.accept(new HeatFrameCoolingRecipe(
                RL("obsidian"),
                Ingredient.fromItems(Items.LAVA_BUCKET),
                new ItemStack(Blocks.OBSIDIAN)
        ));

        hfc.accept(new HeatFrameCoolingRecipe(
                RL("ice"),
                Ingredient.fromItems(Items.WATER_BUCKET),
                new ItemStack(Blocks.ICE)
        ));
    }
}
