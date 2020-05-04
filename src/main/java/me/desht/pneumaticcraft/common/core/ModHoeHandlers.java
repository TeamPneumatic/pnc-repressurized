package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import net.minecraft.item.HoeItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModHoeHandlers {
    @SubscribeEvent
    public static void register(RegistryEvent.Register<HoeHandler> event) {
        event.getRegistry().register(new HoeHandler(
                        item -> item.getItem() instanceof HoeItem,
                        (stack, player) -> stack.damageItem(1, player, p -> { })
                ).setRegistryName("default_hoe_handler")
        );
    }
}
