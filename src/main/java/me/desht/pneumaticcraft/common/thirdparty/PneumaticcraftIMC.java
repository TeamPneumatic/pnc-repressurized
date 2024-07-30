package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.lib.Log;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.ArrayList;

/**
 * Class containing manager for sending all IMC messages to other mods
 */
@EventBusSubscriber(modid = Names.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PneumaticcraftIMC {

    // List of all IMC messages to be sent at the InterModEnqueueEvent stage
    public static final ArrayList<InterModComms.IMCMessage> iMCMessageCache = new ArrayList<>();

    /**
     * Adds the passed IMC message to the IMC message cache
     * @param message IMC message to add to IMC message cache
     */
    public static void addIMCMessageToCache(InterModComms.IMCMessage message) {
        iMCMessageCache.add(message);
    }

    /**
     * Sends all cached IMC messages to other mods
     */
    @SubscribeEvent
    public static void sendIMCMessages(InterModEnqueueEvent event) {
        Log.info("Sending IMC messages.");
        for (InterModComms.IMCMessage message : iMCMessageCache) {
            InterModComms.sendTo(Names.MOD_ID, message.modId(), message.method(), message.messageSupplier());
        }
    }
}