package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.recipes.special.DroneColorCrafting;
import me.desht.pneumaticcraft.common.recipes.special.DroneUpgradeCrafting;
import me.desht.pneumaticcraft.common.recipes.special.GunAmmoPotionCrafting;
import me.desht.pneumaticcraft.common.recipes.special.OneProbeCrafting;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.recipes.special.ShapedFluidCraftingRecipe.SpeedUpgradeCraftingRecipe;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModRecipes {
    public static final IRecipeSerializer<OneProbeCrafting> ONE_PROBE_HELMET_CRAFTING = null;
    public static final IRecipeSerializer<GunAmmoPotionCrafting> GUN_AMMO_POTION_CRAFTING = null;
    public static final IRecipeSerializer<DroneUpgradeCrafting> DRONE_UPGRADE_CRAFTING = null;
    public static final IRecipeSerializer<DroneColorCrafting> DRONE_COLOR_CRAFTING = null;
    public static final IRecipeSerializer<SpeedUpgradeCraftingRecipe> SPEED_UPGRADE_CRAFTING = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(new SpecialRecipeSerializer<>(OneProbeCrafting::new).setRegistryName(RL("one_probe_helmet_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(GunAmmoPotionCrafting::new).setRegistryName(RL("gun_ammo_potion_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(DroneUpgradeCrafting::new).setRegistryName(RL("drone_upgrade_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(DroneColorCrafting::new).setRegistryName(RL("drone_color_crafting")));
            event.getRegistry().register(new SpecialRecipeSerializer<>(SpeedUpgradeCraftingRecipe::new).setRegistryName(RL("speed_upgrade_crafting")));
        }
    }
}
