/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.registry.PNCRegistries;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.amadron.AmadronEventListener;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.commands.ModCommands;
import me.desht.pneumaticcraft.common.config.ConfigHolder;
import me.desht.pneumaticcraft.common.config.subconfig.AuxConfigHandler;
import me.desht.pneumaticcraft.common.config.subconfig.IAuxConfig;
import me.desht.pneumaticcraft.common.dispenser.DroneDispenseBehavior;
import me.desht.pneumaticcraft.common.drone.DroneSpecialVariableHandler;
import me.desht.pneumaticcraft.common.event.MiscEventHandler;
import me.desht.pneumaticcraft.common.event.PneumaticArmorHandler;
import me.desht.pneumaticcraft.common.event.UniversalSensorHandler;
import me.desht.pneumaticcraft.common.fluid.FluidSetup;
import me.desht.pneumaticcraft.common.hacking.HackEventListener;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.heat.behaviour.HeatBehaviourManager;
import me.desht.pneumaticcraft.common.item.GPSAreaToolItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.BlockTrackLootable;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.registry.*;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.upgrades.UpgradesDBSetup;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.util.chunkloading.ForcedChunks;
import me.desht.pneumaticcraft.common.util.chunkloading.PlayerLogoutTracker;
import me.desht.pneumaticcraft.common.villages.VillageStructures;
import me.desht.pneumaticcraft.lib.Log;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@Mod(Names.MOD_ID)
public class PneumaticCraftRepressurized {
    public PneumaticCraftRepressurized(ModContainer container, IEventBus modBus) {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());

        ConfigHolder.init(container, modBus);
        AuxConfigHandler.preInit();

        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.onModConstruction(modBus);
        }

        Reflections.init();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::newRegistries);
        modBus.addListener(ForcedChunks.INSTANCE::registerTicketController);
        modBus.addListener(CapabilitySetup::registerCaps);

        registerAllDeferredRegistryObjects(modBus);

        forgeBus.addListener(this::serverStarted);
        forgeBus.addListener(this::serverStopping);
        forgeBus.addListener(this::addReloadListeners);
        forgeBus.addListener(this::registerCommands);
        forgeBus.register(new MiscEventHandler());
        forgeBus.register(new AmadronEventListener());
        forgeBus.register(new PneumaticArmorHandler());
        forgeBus.register(new UniversalSensorHandler());
        forgeBus.register(new DroneSpecialVariableHandler());
        forgeBus.register(GPSAreaToolItem.EventHandler.class);
        forgeBus.register(HackEventListener.getInstance());
        forgeBus.addListener(VillageStructures::addMechanicHouse);
        forgeBus.register(PlayerLogoutTracker.INSTANCE);
    }

    private void newRegistries(NewRegistryEvent event) {
        // bit kludgy, but this event is fired right after we know for sure which mods are present,
        //   and right before registry init happens
        thirdPartyPreInit();

        event.register(PNCRegistries.HOE_HANDLER_REGISTRY);
        event.register(PNCRegistries.HARVEST_HANDLER_REGISTRY);
        event.register(PNCRegistries.PROG_WIDGETS_REGISTRY);
        event.register(PNCRegistries.PLAYER_MATCHER_REGISTRY);
        event.register(PNCRegistries.AREA_TYPE_SERIALIZER_REGISTRY);
        event.register(PNCRegistries.REMOTE_WIDGETS_REGISTRY);
    }

    private void thirdPartyPreInit() {
        ModList.get().getModContainerById(Names.MOD_ID).ifPresent(pncMod -> {
            ThirdPartyManager.instance().preInit(pncMod.getEventBus());
            if (FMLEnvironment.dist.isClient()) {
                ThirdPartyManager.instance().clientPreInit(pncMod.getEventBus());
            }
        });
    }

    private void registerAllDeferredRegistryObjects(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        ModParticleTypes.PARTICLES.register(modBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        ModConditionSerializers.CONDITIONS.register(modBus);
        ModIngredientTypes.INGREDIENT_TYPES.register(modBus);
        ModRecipeTypes.RECIPE_TYPES.register(modBus);
        ModVillagers.POI.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
        ModCommands.COMMAND_ARGUMENT_TYPES.register(modBus);
        ModPlacementModifierTypes.PLACEMENT_MODIFIERS.register(modBus);
        ModCriterionTriggers.CRITERION_TRIGGERS.register(modBus);
        ModDataComponents.COMPONENTS.register(modBus);
        ModAttachmentTypes.ATTACHMENT_TYPES.register(modBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);

        // custom registries
        ModHarvestHandlers.HARVEST_HANDLERS_DEFERRED.register(modBus);
        ModHoeHandlers.HOE_HANDLERS_DEFERRED.register(modBus);
        ModProgWidgetTypes.PROG_WIDGETS_DEFERRED.register(modBus);
        ModProgWidgetAreaTypes.PROG_WIDGET_AREA_SERIALIZER_DEFERRED.register(modBus);
        ModPlayerMatchers.PLAYER_MATCHERS_DEFERRED.register(modBus);
        ModRemoteWidgetTypes.REMOTE_WIDGETS.register(modBus);

        ModCreativeModeTab.TABS.register(modBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Log.info(Names.MOD_NAME + " is loading!");

        ThirdPartyManager.instance().init();
        FluidSetup.init();
        CommonUpgradeHandlers.init();
        HackManager.addDefaultEntries();
        SensorHandler.getInstance().init();
        ModNameCache.init();
        HeatBehaviourManager.getInstance().registerDefaultBehaviours();
        BlockTrackLootable.INSTANCE.addDefaultEntries();
        ItemLaunching.registerDefaultBehaviours();

        event.enqueueWork(() -> {
            ArmorUpgradeRegistry.getInstance().freeze();
            UpgradesDBSetup.init();
            DroneDispenseBehavior.registerDrones();
            ThirdPartyManager.instance().postInit();
        });
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(PneumaticCraftRecipeType.getCacheReloadListener());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    private void serverStarted(ServerStartedEvent event) {
        AuxConfigHandler.postInit(IAuxConfig.Sidedness.SERVER);
    }

    private void serverStopping(ServerStoppingEvent event) {
        AmadronOfferManager.getInstance().saveAll();

        // if we're on single-player, reset is needed here to stop world-specific configs crossing worlds
        AuxConfigHandler.clearPerWorldConfigs();
    }

}
