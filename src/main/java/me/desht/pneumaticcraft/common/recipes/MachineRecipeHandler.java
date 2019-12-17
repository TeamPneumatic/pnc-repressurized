package me.desht.pneumaticcraft.common.recipes;

import com.google.common.collect.ImmutableMap;
import me.desht.pneumaticcraft.api.recipe.*;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncRecipes;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
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
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationEvents {
        @SubscribeEvent
        public static void onRegister(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            CraftingHelper.register(StackedIngredient.Serializer.ID, StackedIngredient.Serializer.INSTANCE);
        }
    }
}
