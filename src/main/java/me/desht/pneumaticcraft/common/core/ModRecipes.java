package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.common.recipes.ShapedPressurizableRecipe;
import me.desht.pneumaticcraft.common.recipes.special.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModRecipes {
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPES = new DeferredRegister<>(ForgeRegistries.RECIPE_SERIALIZERS, Names.MOD_ID);

    public static final RegistryObject<SpecialRecipeSerializer<OneProbeCrafting>> ONE_PROBE_HELMET_CRAFTING
            = RECIPES.register("one_probe_helmet_crafting", () -> new SpecialRecipeSerializer<>(OneProbeCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<GunAmmoPotionCrafting>> GUN_AMMO_POTION_CRAFTING
            = RECIPES.register("gun_ammo_potion_crafting", () -> new SpecialRecipeSerializer<>(GunAmmoPotionCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<DroneUpgradeCrafting>> DRONE_UPGRADE_CRAFTING
            = RECIPES.register("drone_upgrade_crafting", () -> new SpecialRecipeSerializer<>(DroneUpgradeCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<DroneColorCrafting>> DRONE_COLOR_CRAFTING
            = RECIPES.register("drone_color_crafting", () -> new SpecialRecipeSerializer<>(DroneColorCrafting::new));
    public static final RegistryObject<SpecialRecipeSerializer<PatchouliBookCrafting>> PATCHOULI_BOOK_CRAFTING
            = RECIPES.register("patchouli_book_crafting", () -> new SpecialRecipeSerializer<>(PatchouliBookCrafting::new));
    public static final RegistryObject<ShapedPressurizableRecipe.Serializer> CRAFTING_SHAPED_PRESSURIZABLE
            = RECIPES.register("crafting_shaped_pressurizable", ShapedPressurizableRecipe.Serializer::new);
}
