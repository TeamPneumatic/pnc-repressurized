package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.recipes.ShapedPressurizableRecipe;
import me.desht.pneumaticcraft.common.recipes.special.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModRecipes {
    public static final SpecialRecipeSerializer<OneProbeCrafting> ONE_PROBE_HELMET_CRAFTING = null;
    public static final SpecialRecipeSerializer<GunAmmoPotionCrafting> GUN_AMMO_POTION_CRAFTING = null;
    public static final SpecialRecipeSerializer<DroneUpgradeCrafting> DRONE_UPGRADE_CRAFTING = null;
    public static final SpecialRecipeSerializer<DroneColorCrafting> DRONE_COLOR_CRAFTING = null;
    public static final SpecialRecipeSerializer<PatchouliBookCrafting> PATCHOULI_BOOK_CRAFTING = null;
    public static final IRecipeSerializer<ShapedPressurizableRecipe> CRAFTING_SHAPED_PRESSURIZABLE = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(new SpecialRecipeSerializer<>(OneProbeCrafting::new).setRegistryName(RL("one_probe_helmet_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(GunAmmoPotionCrafting::new).setRegistryName(RL("gun_ammo_potion_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(DroneUpgradeCrafting::new).setRegistryName(RL("drone_upgrade_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(DroneColorCrafting::new).setRegistryName(RL("drone_color_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(PatchouliBookCrafting::new).setRegistryName(RL("patchouli_book_crafting")));
            event.getRegistry().register(new ShapedPressurizableRecipe.Serializer().setRegistryName(RL("crafting_shaped_pressurizable")));
        }
    }
}
