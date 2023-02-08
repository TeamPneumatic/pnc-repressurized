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
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.item.IUpgradeItem;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHacking;
import me.desht.pneumaticcraft.api.tileentity.IAirHandler;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.ClientSetup;
import me.desht.pneumaticcraft.common.PneumaticCraftAPIHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.amadron.AmadronEventListener;
import me.desht.pneumaticcraft.common.amadron.AmadronOfferManager;
import me.desht.pneumaticcraft.common.commands.ModCommands;
import me.desht.pneumaticcraft.common.config.ConfigHolder;
import me.desht.pneumaticcraft.common.config.subconfig.AuxConfigHandler;
import me.desht.pneumaticcraft.common.config.subconfig.IAuxConfig;
import me.desht.pneumaticcraft.common.core.*;
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
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.BlockTrackLootable;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.sensor.SensorHandler;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.util.ItemLaunching;
import me.desht.pneumaticcraft.common.util.PlayerFilter;
import me.desht.pneumaticcraft.common.util.Reflections;
import me.desht.pneumaticcraft.common.util.upgrade.UpgradesDBSetup;
import me.desht.pneumaticcraft.common.villages.VillageStructures;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Names.MOD_ID)
public class PneumaticCraftRepressurized {
    public PneumaticCraftRepressurized() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigHolder.init();
        AuxConfigHandler.preInit();

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::onModConstruction);

        modBus.addListener(this::modConstructSetup);
        modBus.addListener(this::commonSetup);

        forgeBus.addListener(this::serverStarted);
        forgeBus.addListener(this::serverStopping);
        forgeBus.addListener(this::addReloadListeners);
        forgeBus.addListener(this::registerCommands);
        forgeBus.addListener(this::registerCapabilities);

        registerAllDeferredRegistryObjects(modBus);

        Reflections.init();
        PneumaticRegistry.init(PneumaticCraftAPIHandler.getInstance());

        forgeBus.register(new MiscEventHandler());
        forgeBus.register(new AmadronEventListener());
        forgeBus.register(new PneumaticArmorHandler());
        forgeBus.register(new UniversalSensorHandler());
        forgeBus.register(new DroneSpecialVariableHandler());
        forgeBus.register(GPSAreaToolItem.EventHandler.class);
        forgeBus.register(HackEventListener.getInstance());
        forgeBus.addListener(VillageStructures::addMechanicHouse);
    }

    private void registerAllDeferredRegistryObjects(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModFluids.FLUIDS.register(modBus);
        ModFluids.FLUID_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntityTypes.ENTITY_TYPES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);
        ModParticleTypes.PARTICLES.register(modBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modBus);
        ModRecipeTypes.RECIPE_TYPES.register(modBus);
        ModVillagers.POI.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
        ModCommands.COMMAND_ARGUMENT_TYPES.register(modBus);
        ModLootFunctions.LOOT_FUNCTIONS.register(modBus);
        ModPlacementModifierTypes.PLACEMENT_MODIFIERS.register(modBus);

        // custom registries
        ModHarvestHandlers.HARVEST_HANDLERS_DEFERRED.register(modBus);
        ModHoeHandlers.HOE_HANDLERS_DEFERRED.register(modBus);
        ModProgWidgets.PROG_WIDGETS_DEFERRED.register(modBus);
        ModUpgrades.UPGRADES_DEFERRED.register(modBus);
    }

    private void modConstructSetup(FMLConstructModEvent event) {
        ThirdPartyManager.instance().preInit();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Log.info(Names.MOD_NAME + " is loading!");

        ThirdPartyManager.instance().init();
        NetworkHandler.init();
        FluidSetup.init();
        CommonUpgradeHandlers.init();
        HackManager.addDefaultEntries();
        SensorHandler.getInstance().init();
        ModNameCache.init();
        HeatBehaviourManager.getInstance().registerDefaultBehaviours();
        PlayerFilter.registerDefaultMatchers();
        BlockTrackLootable.INSTANCE.addDefaultEntries();
        ItemLaunching.registerDefaultBehaviours();

        event.enqueueWork(() -> {
            ArmorUpgradeRegistry.getInstance().freeze();
            UpgradesDBSetup.init();
            AdvancementTriggers.registerTriggers();
            DroneDispenseBehavior.registerDrones();
            ThirdPartyManager.instance().postInit();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IAirHandler.class);
        event.register(IAirHandlerItem.class);
        event.register(IAirHandlerMachine.class);
        event.register(IHeatExchangerLogic.class);
        event.register(IHacking.class);
        event.register(IUpgradeItem.class);
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
