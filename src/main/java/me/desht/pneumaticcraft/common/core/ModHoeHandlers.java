package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.item.HoeItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModHoeHandlers {
    @Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void register(RegistryEvent.Register<HoeHandler> event) {
            // matches any vanilla hoe and any modded hoe which extends HoeItem
            event.getRegistry().register(new HoeHandler(item -> item.getItem() instanceof HoeItem,
                    (stack, player) -> stack.damageItem(1, player, p -> { })).setRegistryName(RL("default_hoe_handler")));
        }
    }
}
