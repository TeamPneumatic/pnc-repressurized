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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.client.gui.IGuiDrone;
import pneumaticCraft.client.render.RenderProgressingLine;
import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.DateEventHandler;
import pneumaticCraft.common.block.tubes.ModuleRegulatorTube;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemMinigun;
import pneumaticCraft.common.item.ItemProgrammingPuzzle;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.minigun.Minigun;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientEventHandler{
    public static float playerRenderPartialTick;
    private static boolean firstTick = true;
    private final RenderProgressingLine minigunFire = new RenderProgressingLine().setProgress(1);

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

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event){
        double gunRadius = 1.1D;

        EntityPlayer thisPlayer = Minecraft.getMinecraft().thePlayer;
        double playerX = thisPlayer.prevPosX + (thisPlayer.posX - thisPlayer.prevPosX) * event.partialTicks;
        double playerY = thisPlayer.prevPosY + (thisPlayer.posY - thisPlayer.prevPosY) * event.partialTicks;
        double playerZ = thisPlayer.prevPosZ + (thisPlayer.posZ - thisPlayer.prevPosZ) * event.partialTicks;
        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);

        for(EntityPlayer player : (List<EntityPlayer>)Minecraft.getMinecraft().theWorld.playerEntities) {
            if(thisPlayer == player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) continue;
            ItemStack curItem = player.getCurrentEquippedItem();
            if(curItem != null && curItem.getItem() == Itemss.minigun) {
                Minigun minigun = ((ItemMinigun)Itemss.minigun).getMinigun(curItem, player);
                if(minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                    GL11.glPushMatrix();
                    playerX = player.prevPosX + (player.posX - player.prevPosX) * event.partialTicks;
                    playerY = player.prevPosY + (player.posY - player.prevPosY) * event.partialTicks;
                    playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.partialTicks;
                    GL11.glTranslated(playerX, playerY + 0.5, playerZ);

                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    //GL11.glDisable(GL11.GL_LIGHTING);
                    RenderUtils.glColorHex(0xFF000000 | minigun.getAmmoColor());
                    for(int i = 0; i < 5; i++) {

                        Vec3 directionVec = player.getLookVec().normalize();
                        Vec3 vec = Vec3.createVectorHelper(directionVec.xCoord, 0, directionVec.zCoord).normalize();
                        vec.rotateAroundY((float)Math.toRadians(-15 + (player.rotationYawHead - player.renderYawOffset)));
                        minigunFire.startX = vec.xCoord * gunRadius;
                        minigunFire.startY = vec.yCoord * gunRadius - player.yOffset;
                        minigunFire.startZ = vec.zCoord * gunRadius;
                        minigunFire.endX = directionVec.xCoord * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endY = directionVec.yCoord * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endZ = directionVec.zCoord * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.render();
                    }
                    GL11.glColor4d(1, 1, 1, 1);
                    // GL11.glEnable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glPopMatrix();
                }
            }
        }
        GL11.glPopMatrix();
    }
}
