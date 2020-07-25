package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class TickHandlerPneumaticCraft {

    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            World world = event.world;
            DroneClaimManager.getInstance(world).update();
            if (event.world.getGameTime() % 100 == 0) {
                double tickTime = MathHelper.average(ServerLifecycleHooks.getCurrentServer().tickTimeArray) * 1.0E-6D;
                // In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.func_234923_W_());
            }
        }
    }

    @SubscribeEvent
    public void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            int ticks = ServerLifecycleHooks.getCurrentServer().getTickCounter();
            if (ticks % PNCConfig.Common.Amadron.reshuffleInterval == PNCConfig.Common.Amadron.reshuffleInterval - 1) {
                AmadronOfferManager.getInstance().compileActiveOffersList();
            }
            if (ticks % 600 == 0) {
                AmadronOfferManager.getInstance().tryRestockPlayerOffers();
            }
        }
    }
}
