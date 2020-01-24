package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import me.desht.pneumaticcraft.api.crafting.*;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.network.PacketSyncRecipes;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class MachineRecipeHandler {
    private static final String MACHINE_RECIPES = "pneumaticcraft/machine_recipes/";
    private static final int JSON_EXTENSION_LENGTH = ".json".length();

    public enum Category {
        PRESSURE_CHAMBER("pressure_chamber"),
        THERMO_PLANT("thermopneumatic_processing_plant"),
        HEAT_FRAME_COOLING("heat_frame_cooling"),
        REFINERY("refinery"),
        EXPLOSION_CRAFTING("explosion_crafting"),
        ASSEMBLY("assembly");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public ResourceLocation getId() { return RL(name); }
    }

    public static class ReloadListener implements ISelectiveResourceReloadListener {
        private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        private List<Map<ResourceLocation, JsonObject>> allRecipes = new ArrayList<>();

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
            allRecipes.clear();
            for (Category cat : Category.values()) {
                allRecipes.add(loadJSON(resourceManager, cat));
            }

            Map<ResourceLocation, IPressureChamberRecipe> pressureChamber = parseJSON(Category.PRESSURE_CHAMBER);
            Map<ResourceLocation, IThermopneumaticProcessingPlantRecipe> thermopneumatic = parseJSON(Category.THERMO_PLANT);
            Map<ResourceLocation, IHeatFrameCoolingRecipe> heatFrameCooling = parseJSON(Category.HEAT_FRAME_COOLING);
            Map<ResourceLocation, IExplosionCraftingRecipe> explosionCrafting = parseJSON(Category.EXPLOSION_CRAFTING);
            Map<ResourceLocation, IRefineryRecipe> refinery = parseJSON(Category.REFINERY);
            Map<ResourceLocation, IAssemblyRecipe> assembly = parseJSON(Category.ASSEMBLY);

            // post event to give mods a chance to modify recipes in code (do we want to keep this?)
            RegisterMachineRecipesEvent evt = new RegisterMachineRecipesEvent(
                    r -> pressureChamber.put(r.getId(), r),
                    r -> thermopneumatic.put(r.getId(), r),
                    r -> heatFrameCooling.put(r.getId(), r),
                    r -> explosionCrafting.put(r.getId(), r),
                    r -> refinery.put(r.getId(), r),
                    r -> assembly.put(r.getId(), r)
            );
            MinecraftForge.EVENT_BUS.post(evt);

            PneumaticCraftRecipes.pressureChamberRecipes = ImmutableMap.copyOf(pressureChamber);
            PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes = ImmutableMap.copyOf(thermopneumatic);
            PneumaticCraftRecipes.heatFrameCoolingRecipes = ImmutableMap.copyOf(heatFrameCooling);
            PneumaticCraftRecipes.explosionCraftingRecipes = ImmutableMap.copyOf(explosionCrafting);
            PneumaticCraftRecipes.refineryRecipes = ImmutableMap.copyOf(refinery);
            AssemblyRecipe.setupRecipeSubtypes(assembly.values());

            AmadronOfferManager.getInstance().initOffers();

            NetworkHandler.sendToAll(syncPacket());
        }

        /**
         * Load all JSON recipes for the given category, searching all known datapacks. If an empty recipe JSON is
         * found for an existing recipe, it will be removed; this is how datapacks can disable default recipes.
         *
         * @param resourceManager the resource manager
         * @param category a machine recipe category
         * @return a map (recipeID -> json) of all discovered recipes for the category
         */
        private Map<ResourceLocation, JsonObject> loadJSON(IResourceManager resourceManager, Category category) {
            Map<ResourceLocation, JsonObject> map = new HashMap<>();
            String folder = MACHINE_RECIPES + category.getName();
            int l = folder.length() + 1;
            for (ResourceLocation file : resourceManager.getAllResourceLocations(folder, r -> r.endsWith(".json"))) {
                String path = file.getPath();
                ResourceLocation id = new ResourceLocation(file.getNamespace(), path.substring(l, path.length() - JSON_EXTENSION_LENGTH));
                Log.debug("loading %s recipe: %s", category.getName(), id);
                try (IResource iresource = resourceManager.getResource(file);
                     InputStream inputstream = iresource.getInputStream();
                     Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8)))
                {
                    JsonObject jsonobject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
                    if (jsonobject != null) {
                        if (jsonobject.size() == 0) {
                            if (map.containsKey(id)) {
                                Log.debug("removing %s recipe ID %s (found empty recipe JSON)", category.getName(), id);
                                map.remove(id);
                            }
                        } else {
                            JsonObject j = map.put(id, jsonobject);
                            if (j != null) {
                                Log.error("duplicate data file ignored with ID " + id);
                            }
                        }
                    } else {
                        Log.error("can't load data file %s from %s as it's null or empty", id, file);
                    }
                } catch (IllegalArgumentException | IOException | JsonParseException e) {
                    Log.error("can't parse data file %s (%s) - stack trace follows:", file, e.getMessage());
                    Log.error(ExceptionUtils.getStackTrace(e));
                }
            }
            return map;
        }

        /**
         * Parse all the recipes JSONs in the given category and create the actual recipe objects.
         *
         * @param category the category
         * @return a map of (recipeID -> recipe object)
         */
        private <T extends IModRecipe> Map<ResourceLocation, T> parseJSON(Category category) {
            Map<ResourceLocation, T> result = new HashMap<>();
            allRecipes.get(category.ordinal()).forEach((recipeId, json) -> {
                try {
                    ResourceLocation type = new ResourceLocation(
                            JSONUtils.getString(json, "type", Names.MOD_ID + ":" + category.getName()));
                    IModRecipeSerializer<T> serializer = ModCraftingHelper.getSerializer(type);
                    if (serializer != null) {
                        T recipe = serializer.read(recipeId, json);
                        if (recipe != null) {
                            result.put(recipeId, recipe);
                        }
                    } else {
                        Log.error("can't deserialize recipe %s - no deserializer for %s", recipeId, type);
                    }
                } catch (JsonParseException e) {
                    Log.error("can't deserialize %s recipe %s (%s) - stack trace follows:", category.getName(), recipeId, e.getMessage());
                    Log.error(ExceptionUtils.getStackTrace(e));
                }
            });
            return result;
        }
    }

    private static PacketSyncRecipes syncPacket() {
        return new PacketSyncRecipes(
                PneumaticCraftRecipes.pressureChamberRecipes,
                PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes,
                PneumaticCraftRecipes.heatFrameCoolingRecipes,
                PneumaticCraftRecipes.explosionCraftingRecipes,
                PneumaticCraftRecipes.refineryRecipes,
                PneumaticCraftRecipes.assemblyLaserRecipes,
                PneumaticCraftRecipes.assemblyDrillRecipes,
                PneumaticCraftRecipes.assemblyLaserDrillRecipes
        );
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void clientLogout(ClientPlayerNetworkEvent.LoggedOutEvent evt) {
            PneumaticCraftRecipes.pressureChamberRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.thermopneumaticProcessingPlantRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.heatFrameCoolingRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.explosionCraftingRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.refineryRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.assemblyLaserRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.assemblyDrillRecipes = Collections.emptyMap();
            PneumaticCraftRecipes.assemblyLaserDrillRecipes = Collections.emptyMap();
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void serverLogin(PlayerEvent.PlayerLoggedInEvent evt) {
            NetworkHandler.sendNonLocal((ServerPlayerEntity) evt.getPlayer(), syncPacket());
            NetworkHandler.sendNonLocal((ServerPlayerEntity) evt.getPlayer(), new PacketSyncAmadronOffers());
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationEvents {
        @SubscribeEvent
        public static void onRegister(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            CraftingHelper.register(StackedIngredient.Serializer.ID, StackedIngredient.Serializer.INSTANCE);
            CraftingHelper.register(FluidIngredient.Serializer.ID, FluidIngredient.Serializer.INSTANCE);

            ModCraftingHelper.register(Category.ASSEMBLY.getId(), AssemblyRecipe.Serializer::new);
            ModCraftingHelper.register(Category.PRESSURE_CHAMBER.getId(), BasicPressureChamberRecipe.Serializer::new);
            ModCraftingHelper.register(Category.THERMO_PLANT.getId(), BasicThermopneumaticProcessingPlantRecipe.Serializer::new);
            ModCraftingHelper.register(Category.EXPLOSION_CRAFTING.getId(), ExplosionCraftingRecipe.Serializer::new);
            ModCraftingHelper.register(Category.HEAT_FRAME_COOLING.getId(), HeatFrameCoolingRecipe.Serializer::new);
            ModCraftingHelper.register(Category.REFINERY.getId(), RefineryRecipe.Serializer::new);

            ModCraftingHelper.register(PressureChamberEnchantingRecipe.ID, PressureChamberEnchantingRecipe.Serializer::new);
            ModCraftingHelper.register(PressureChamberDisenchantingRecipe.ID, PressureChamberDisenchantingRecipe.Serializer::new);
        }
    }
}
