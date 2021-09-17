package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class TickHandlerPneumaticCraft {
    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide) {

            DroneClaimManager.getInstance(event.world).update();

            if (event.world.getGameTime() % 100 == 0) {
                double tickTime = MathHelper.average(ServerLifecycleHooks.getCurrentServer().tickTimes) * 1.0E-6D;
                // In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.dimension());
            }
        }
    }
}
