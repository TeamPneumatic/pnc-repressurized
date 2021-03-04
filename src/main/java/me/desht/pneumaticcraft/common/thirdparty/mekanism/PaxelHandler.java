package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Names.MOD_ID)
public class PaxelHandler extends HoeHandler {
    public PaxelHandler() {
        super(stack -> {
            ResourceLocation rl = stack.getItem().getRegistryName();
            return rl != null && rl.getNamespace().equals("mekanismtools") && rl.getPath().endsWith("_paxel");
        }, (stack, player) -> stack.damageItem(1, player, p -> { }));
    }

    @SubscribeEvent
    public static void registerPaxelHandler(RegistryEvent.Register<HoeHandler> event) {
        event.getRegistry().register(new PaxelHandler().setRegistryName(RL("mekanism_paxels")));
    }
}
