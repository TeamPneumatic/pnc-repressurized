package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.INeedTickUpdate;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ClientTickHandler {
    private static final ClientTickHandler INSTANCE = new ClientTickHandler();
    public static int TICKS;

    private final List<WeakReference<INeedTickUpdate>> updatedObjects = new ArrayList<WeakReference<INeedTickUpdate>>();//using weak references so we don't create a memory leak of unused GuiAnimatedStats.

    public static ClientTickHandler instance() {
        return INSTANCE;
    }

    /**
     * Invoking this method will result the given stat to be updated every tick.
     *
     * @param stat
     */
    public void registerUpdatedObject(INeedTickUpdate stat) {
        updatedObjects.add(new WeakReference<INeedTickUpdate>(stat));
    }

    /**
     * Method used to force an object to not get updates any longer. When further updates aren't harmful when the object
     * not longer is needed, this method isn't necessary to be used, as the garbage collector will collect the
     * (weak referenced) objects.
     *
     * @param stat
     */
    public void removeUpdatedObject(INeedTickUpdate stat) {
        for (int i = 0; i < updatedObjects.size(); i++) {
            if (stat.equals(updatedObjects.get(i).get())) {
                updatedObjects.remove(i);
                break;
            }
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (Minecraft.getMinecraft().player == null) {
                for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().upgradeRenderers) {
                    handler.reset();
                }
            }
            TICKS++;
            ModuleRegulatorTube.hasTicked = false;
            ModuleRegulatorTube.inverted = false;
            ModuleRegulatorTube.inLine = true;
            for (int i = 0; i < updatedObjects.size(); i++) {
                INeedTickUpdate updatedObject = updatedObjects.get(i).get();
                if (updatedObject != null) {
                    updatedObject.update();
                } else {
                    updatedObjects.remove(i);
                    i--;
                }
            }
        }
    }

    private boolean firstTick = true;

    @SubscribeEvent
    public void onPlayerJoin(TickEvent.PlayerTickEvent event) {
        if (firstTick && event.player.world.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity()) {
            event.player.sendStatusMessage(new TextComponentString(
                    TextFormatting.RED + "PneumaticCraft is unstable at this point! A few blocks/items " +
                            TextFormatting.RED + "don't have a proper model yet. THIS IS NOT A BUG. Most " +
                            TextFormatting.RED + "features should work though (regardless of how it's " +
                            TextFormatting.RED + "displayed as of now)."), false);
            firstTick = false;
        }
    }
}
