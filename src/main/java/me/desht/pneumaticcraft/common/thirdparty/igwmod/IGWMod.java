package me.desht.pneumaticcraft.common.thirdparty.igwmod;

import igwmod.lib.Constants;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class IGWMod implements IThirdParty {

    @Override
    public void preInit() {
        try {
            int minorVersion = Integer.parseInt(ReflectionHelper.getPrivateValue(Constants.class, null, "MINOR"));
            if (minorVersion < 7) MinecraftForge.EVENT_BUS.register(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public void clientSide() {
        FMLInterModComms.sendMessage("IGWMod", "pneumaticCraft.common.thirdparty.igwmod.IGWHandler", "init");
    }

    @Override
    public void clientInit() {
    }

    @SubscribeEvent
    public void notifyOutdatedIGW(TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity()) {
            event.player.sendStatusMessage(new TextComponentString(
                    TextFormatting.RED + "You are running an outdated version of IGW-Mod which will not work properly with PneumaticCraft!:( Please update!"), false);
            FMLCommonHandler.instance().bus().unregister(this);
        }
    }
}
