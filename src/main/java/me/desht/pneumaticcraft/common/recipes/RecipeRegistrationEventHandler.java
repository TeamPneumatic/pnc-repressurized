package me.desht.pneumaticcraft.common.recipes;

import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.ingredient.StackedIngredient;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RecipeRegistrationEventHandler {
    @SubscribeEvent
    public static void onRegister(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        // register our custom recipe and ingredient types

        PneumaticCraftRecipeType.registerRecipeTypes(event.getRegistry());

        CraftingHelper.register(StackedIngredient.Serializer.ID, StackedIngredient.Serializer.INSTANCE);
        CraftingHelper.register(FluidIngredient.Serializer.ID, FluidIngredient.Serializer.INSTANCE);
    }
}
