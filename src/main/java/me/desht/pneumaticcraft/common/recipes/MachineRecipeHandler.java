package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.crafting.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.RegisterMachineRecipesEvent;
import me.desht.pneumaticcraft.api.crafting.StackedIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.*;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.network.PacketSyncRecipes;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.recipes.machine.*;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.resources.IResourceManager;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MachineRecipeHandler {
    public static class ReloadListener implements ISelectiveResourceReloadListener {
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
            Map<ResourceLocation, IPressureChamberRecipe> pressureChamber = new HashMap<>();
            Map<ResourceLocation, IThermopneumaticProcessingPlantRecipe> thermopneumatic = new HashMap<>();
            Map<ResourceLocation, IHeatFrameCoolingRecipe> heatFrameCooling = new HashMap<>();
            Map<ResourceLocation, IExplosionCraftingRecipe> explosionCrafting = new HashMap<>();
            Map<ResourceLocation, IRefineryRecipe> refinery = new HashMap<>();
            Map<ResourceLocation, IAssemblyRecipe> assembly = new HashMap<>();

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

            ModCraftingHelper.registerSerializer(AssemblyRecipe.RECIPE_TYPE, AssemblyRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(BasicPressureChamberRecipe.RECIPE_TYPE, BasicPressureChamberRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(PressureChamberEnchantingRecipe.ID, PressureChamberEnchantingRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(PressureChamberDisenchantingRecipe.ID, PressureChamberDisenchantingRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(BasicThermopneumaticProcessingPlantRecipe.RECIPE_TYPE, BasicThermopneumaticProcessingPlantRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(ExplosionCraftingRecipe.RECIPE_TYPE, ExplosionCraftingRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(HeatFrameCoolingRecipe.RECIPE_TYPE, HeatFrameCoolingRecipe.Serializer::new);
            ModCraftingHelper.registerSerializer(RefineryRecipe.RECIPE_TYPE, RefineryRecipe.Serializer::new);
        }
    }
}
