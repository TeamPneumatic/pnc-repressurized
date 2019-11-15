package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryBuilder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModRegistries {
    public static ForgeRegistry<HarvestHandler> HARVEST_HANDLERS = null;
    public static ForgeRegistry<HoeHandler> HOE_HANDLERS = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegistryEvent.NewRegistry event) {
            HARVEST_HANDLERS = (ForgeRegistry<HarvestHandler>) new RegistryBuilder<HarvestHandler>()
                    .setName(RL("harvest_handlers"))
                    .setIDRange(1, Integer.MAX_VALUE)
                    .setType(HarvestHandler.class)
                    .disableSaving()
                    .create();

            HOE_HANDLERS = (ForgeRegistry<HoeHandler>) new RegistryBuilder<HoeHandler>()
                    .setName(RL("hoe_handlers"))
                    .setIDRange(1, Integer.MAX_VALUE)
                    .setType(HoeHandler.class)
                    .disableSaving()
                    .create();
        }
    }
}
