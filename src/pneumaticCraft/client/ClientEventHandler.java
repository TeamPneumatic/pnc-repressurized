package pneumaticCraft.client;

import java.util.Map;

import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.Fluids;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler{
    public static float playerRenderPartialTick;

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
        if(event.itemStack.getItem() instanceof IProgrammable) {
            IProgrammable programmable = (IProgrammable)event.itemStack.getItem();
            if(programmable.canProgram(event.itemStack) && programmable.showProgramTooltip()) {
                Map<String, Integer> widgetMap = TileEntityProgrammer.getPuzzleSummary(TileEntityProgrammer.getProgWidgets(event.itemStack));
                for(Map.Entry<String, Integer> entry : widgetMap.entrySet()) {
                    event.toolTip.add("-" + entry.getValue() + "x " + ItemProgrammingPuzzle.getStackForWidgetKey(entry.getKey()).getTooltip(event.entityPlayer, false).get(1));
                }
            }
        }
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Post event){
        Fluids.EtchAcid.setIcons(Blockss.etchingAcid.getIcon(0, 0), Blockss.etchingAcid.getIcon(1, 0));
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Specials.Pre event){
        //      event.renderHelmet = false;
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event){
        playerRenderPartialTick = event.partialRenderTick;
        if(event.entityPlayer.getCurrentArmor(3) == null || event.entityPlayer.getCurrentArmor(3).getItem() != Itemss.pneumaticHelmet) return;
        event.renderer.modelBipedMain.bipedHead.showModel = false;
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event){
        event.renderer.modelBipedMain.bipedHead.showModel = true;
    }
}
