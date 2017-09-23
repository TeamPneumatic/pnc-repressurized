package igwmod.render;

import org.lwjgl.input.Keyboard;

import igwmod.ClientProxy;
import igwmod.ConfigHandler;
import igwmod.IGWMod;
import igwmod.TickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TooltipOverlayHandler{

    @SubscribeEvent
    public void tickEnd(TickEvent.RenderTickEvent event){
        if(event.phase == TickEvent.Phase.END && TickHandler.showTooltip() && ConfigHandler.shouldShowTooltip && FMLClientHandler.instance().getClient().inGameHasFocus && IGWMod.proxy.getPlayer().world != null) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            ScaledResolution sr = new ScaledResolution(mc);
            FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            String objectName = TickHandler.getCurrentObjectName();
            String moreInfo = "'" + Keyboard.getKeyName(ClientProxy.openInterfaceKey.getKeyCode()) + "' for more info";
            fontRenderer.drawString(objectName, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(objectName) / 2, sr.getScaledHeight() / 2 - 20, 0xFFFFFFFF);
            fontRenderer.drawString(moreInfo, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(moreInfo) / 2, sr.getScaledHeight() / 2 - 10, 0xFFFFFFFF);
        }
    }
}
