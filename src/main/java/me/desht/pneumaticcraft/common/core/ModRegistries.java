package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModRegistries {
    public static IForgeRegistry<HarvestHandler> HARVEST_HANDLERS = null;
    public static IForgeRegistry<HoeHandler> HOE_HANDLERS = null;
    public static IForgeRegistry<ProgWidgetType<?>> PROG_WIDGETS = null;

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegistryEvent.NewRegistry event) {
            HARVEST_HANDLERS = makeRegistry("harvest_handlers", HarvestHandler.class).create();
            HOE_HANDLERS = makeRegistry("hoe_handlers", HoeHandler.class).create();

            //noinspection unchecked
            makeRegistry("prog_widgets", ProgWidgetType.class).create();
            PROG_WIDGETS = RegistryManager.ACTIVE.getRegistry(RL("prog_widgets"));
        }

        private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(String name, Class<T> type) {
            return new RegistryBuilder<T>().setName(RL(name)).setType(type).setMaxID(Integer.MAX_VALUE - 1).disableSaving();
        }
    }
}
