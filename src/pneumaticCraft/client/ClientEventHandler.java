package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.Config;
import pneumaticCraft.common.DateEventHandler;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientEventHandler{
    public static float playerRenderPartialTick;

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
        if(event.itemStack.getItem() instanceof IProgrammable) {
            IProgrammable programmable = (IProgrammable)event.itemStack.getItem();
            if(programmable.canProgram(event.itemStack) && programmable.showProgramTooltip()) {
                List<String> addedEntries = new ArrayList<String>();
                Map<String, Integer> widgetMap = getPuzzleSummary(TileEntityProgrammer.getProgWidgets(event.itemStack));
                for(Map.Entry<String, Integer> entry : widgetMap.entrySet()) {
                    addedEntries.add("-" + entry.getValue() + "x " + I18n.format("programmingPuzzle." + entry.getKey() + ".name"));
                }
                Collections.sort(addedEntries);
                event.toolTip.addAll(addedEntries);
            }
        }
    }

    private static Map<String, Integer> getPuzzleSummary(List<IProgWidget> widgets){
        Map<String, Integer> map = new HashMap<String, Integer>();
        for(IProgWidget widget : widgets) {
            if(!map.containsKey(widget.getWidgetString())) {
                map.put(widget.getWidgetString(), 1);
            } else {
                map.put(widget.getWidgetString(), map.get(widget.getWidgetString()) + 1);
            }
        }
        return map;
    }

    @SubscribeEvent
    public void onTextureStitchEventPost(TextureStitchEvent.Post event){
        Fluids.EtchAcid.setIcons(Blockss.etchingAcid.getIcon(0, 0), Blockss.etchingAcid.getIcon(1, 0));

    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre event){
        if(event.map.getTextureType() == 0) {
            Fluids.plastic.setIcons(event.map.registerIcon("pneumaticcraft:plastic_still"), event.map.registerIcon("pneumaticcraft:plastic_flow"));
        }
    }

    @SubscribeEvent
    public void onLivingRender(RenderLivingEvent.Pre event){
        setRenderHead(event.entity, false);
    }

    @SubscribeEvent
    public void onLivingRender(RenderLivingEvent.Post event){
        setRenderHead(event.entity, true);
    }

    private void setRenderHead(EntityLivingBase entity, boolean setRender){
        if(entity.getEquipmentInSlot(4) != null && entity.getEquipmentInSlot(4).getItem() == Itemss.pneumaticHelmet && (Config.useHelmetModel || DateEventHandler.isIronManEvent())) {
            Render renderer = RenderManager.instance.getEntityRenderObject(entity);
            if(renderer instanceof RenderBiped) {
                ((RenderBiped)renderer).modelBipedMain.bipedHead.showModel = setRender;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Pre event){
        playerRenderPartialTick = event.partialRenderTick;
        if(!Config.useHelmetModel && !DateEventHandler.isIronManEvent() || event.entityPlayer.getCurrentArmor(3) == null || event.entityPlayer.getCurrentArmor(3).getItem() != Itemss.pneumaticHelmet) return;
        event.renderer.modelBipedMain.bipedHead.showModel = false;
    }

    @SubscribeEvent
    public void onPlayerRender(RenderPlayerEvent.Post event){
        event.renderer.modelBipedMain.bipedHead.showModel = true;
    }
}
