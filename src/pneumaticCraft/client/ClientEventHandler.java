package pneumaticCraft.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.Fluid;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.client.gui.IGuiDrone;
import pneumaticCraft.common.DateEventHandler;
import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.fluid.Fluids;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientEventHandler{
    public static float playerRenderPartialTick;
    private static boolean firstTick = true;

    @SubscribeEvent
    public void onPlayerJoin(TickEvent.PlayerTickEvent event){
        if(Config.shouldDisplayChangeNotification && firstTick && event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity()) {
            event.player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[PneumaticCraft] Disabled world generation of plants and plant mob drops in your config automatically, oil is turned on as replacement. This is only done once, you can change it as you wish now."));
            firstTick = false;
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event){
        if(event.itemStack.getItem() instanceof IProgrammable) {
            IProgrammable programmable = (IProgrammable)event.itemStack.getItem();
            if(programmable.canProgram(event.itemStack) && programmable.showProgramTooltip()) {
                boolean hasInvalidPrograms = false;
                List<String> addedEntries = new ArrayList<String>();
                Map<String, Integer> widgetMap = getPuzzleSummary(TileEntityProgrammer.getProgWidgets(event.itemStack));
                for(Map.Entry<String, Integer> entry : widgetMap.entrySet()) {
                    IProgWidget widget = ItemProgrammingPuzzle.getWidgetForName(entry.getKey());
                    String prefix = "";
                    GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
                    if(curScreen instanceof IGuiDrone) {
                        if(!((IGuiDrone)curScreen).getDrone().isProgramApplicable(widget)) {
                            prefix = EnumChatFormatting.RED + "";
                            hasInvalidPrograms = true;
                        }
                    }
                    addedEntries.add(prefix + "-" + entry.getValue() + "x " + I18n.format("programmingPuzzle." + entry.getKey() + ".name"));
                }
                if(hasInvalidPrograms) {
                    event.toolTip.add(EnumChatFormatting.RED + I18n.format("gui.tooltip.programmable.invalidPieces"));
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
        for(int i = 0; i < Fluids.fluids.size(); i++) {
            if(Fluids.nativeFluids.get(i)) {
                Fluid fluid = Fluids.fluids.get(i);
                fluid.setIcons(fluid.getBlock().getIcon(0, 0), fluid.getBlock().getIcon(1, 0));
                //fluid.setIcons(event.map.registerIcon("pneumaticcraft:" + fluid.getName() + "_still"), event.map.registerIcon("pneumaticcraft:" + fluid.getName() + "_flow"));
            }
        }

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

    @SubscribeEvent
    public void tickEnd(TickEvent.RenderTickEvent event){
        if(event.phase == TickEvent.Phase.END && FMLClientHandler.instance().getClient().inGameHasFocus && PneumaticCraft.proxy.getPlayer().worldObj != null && (ModuleRegulatorTube.inverted || !ModuleRegulatorTube.inLine)) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
            String warning = EnumChatFormatting.RED + I18n.format("gui.regulatorTube.hudMessage." + (ModuleRegulatorTube.inverted ? "inverted" : "notInLine"));
            fontRenderer.drawStringWithShadow(warning, sr.getScaledWidth() / 2 - fontRenderer.getStringWidth(warning) / 2, sr.getScaledHeight() / 2 + 30, 0xFFFFFFFF);
        }
    }
}
