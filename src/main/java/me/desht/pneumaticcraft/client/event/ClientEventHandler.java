package me.desht.pneumaticcraft.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.IExtraGuiHandling;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRegulatorTube;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.event.DateEventHandler;
import me.desht.pneumaticcraft.common.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsActivate;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {
    private static final double MINIGUN_RADIUS = 1.1D;
    private static final double MINIGUN_TEXT_SIZE = 0.55D;
    private static final float MAX_SCREEN_ROLL = 25F;  // max roll in degrees when flying with jetboots

    private static float currentScreenRoll = 0F;

    private final static ProgressingLine minigunFire = new ProgressingLine().setProgress(1);
    private static int lastWidth = -1;
    private static int lastHeight = -1;

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Pre event) {
        setRenderHead(event.getEntity(), false);
    }

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Post event) {
        setRenderHead(event.getEntity(), true);
    }

    private static void setRenderHead(LivingEntity entity, boolean setRender) {
        if (entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() == ModItems.PNEUMATIC_HELMET.get()
                && (PNCConfig.Client.Armor.fancyArmorModels || DateEventHandler.isIronManEvent())) {
            EntityRenderer renderer = Minecraft.getInstance().getRenderManager().getRenderer(entity);
            if (renderer instanceof BipedRenderer) {
                BipedModel modelBiped = (BipedModel) ((BipedRenderer) renderer).getEntityModel();
                modelBiped.bipedHead.showModel = setRender;
            }
        }
    }

    /* TODO 1.8 @SubscribeEvent
      public void onPlayerRender(RenderPlayerEvent.Pre event){
          playerRenderPartialTick = event.partialRenderTick;
          if(!Config.useHelmetModel && !DateEventHandler.isIronManEvent() || event.entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null || event.entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() != Itemss.pneumaticHelmet) return;
          event.renderer.modelBipedMain.bipedHead.showModel = false;
      }

      @SubscribeEvent
      public void onPlayerRender(RenderPlayerEvent.Post event){
          event.renderer.modelBipedMain.bipedHead.showModel = true;
      }*/

    @SubscribeEvent
    public static void tickEnd(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().isGameFocused() 
                && ClientUtils.getClientPlayer() != null
                && (ModuleRegulatorTube.inverted || !ModuleRegulatorTube.inLine)) {
            Minecraft mc = Minecraft.getInstance();
            MainWindow mw = mc.getMainWindow();
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            String warning = TextFormatting.RED + I18n.format("gui.regulatorTube.hudMessage." + (ModuleRegulatorTube.inverted ? "inverted" : "notInLine"));
            fontRenderer.drawStringWithShadow(warning, mw.getScaledWidth() / 2f - fontRenderer.getStringWidth(warning) / 2f, mw.getScaledHeight() / 2f + 30, 0xFFFFFFFF);
        }
    }

    @SubscribeEvent
    public static void renderFirstPersonMinigun(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && Minecraft.getInstance().gameSettings.thirdPersonView == 0) {
            PlayerEntity player = Minecraft.getInstance().player;
            Minecraft mc = Minecraft.getInstance();
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.getItem() instanceof ItemMinigun){
                Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
                int w = event.getWindow().getScaledWidth();
                int h = event.getWindow().getScaledHeight();

                if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                     drawBulletTraces2D(minigun.getAmmoColor() | 0x40000000, w, h);
                }

                ItemStack ammo = minigun.getAmmoStack();
                if (!ammo.isEmpty()) {
                    GuiUtils.drawItemStack(ammo,w / 2 + 16, h / 2 - 7);
                    int remaining = ammo.getMaxDamage() - ammo.getDamage();
                    RenderSystem.pushMatrix();
                    RenderSystem.translated(w / 2f + 32, h / 2f - 1, 0);
                    RenderSystem.scaled(MINIGUN_TEXT_SIZE, MINIGUN_TEXT_SIZE, 1.0);
                    String text = remaining + "/" + ammo.getMaxDamage();
                    mc.fontRenderer.drawString(text, 1, 0, 0);
                    mc.fontRenderer.drawString(text, -1, 0, 0);
                    mc.fontRenderer.drawString(text, 0, 1, 0);
                    mc.fontRenderer.drawString(text, 0, -1, 0);
                    mc.fontRenderer.drawString(text, 0, 0, minigun.getAmmoColor());
                    RenderSystem.popMatrix();
                }
                mc.getTextureManager().bindTexture(Textures.MINIGUN_CROSSHAIR);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.color4f(0.2f, 1.0f, 0.2f, 0.6f);
                AbstractGui.blit(w / 2 - 7, h / 2 - 7, 0, 0, 16, 16, 16, 16);
                event.setCanceled(true);
            }
        }
    }

    private static void drawBulletTraces2D(int color, int w, int h) {
        RenderSystem.pushMatrix();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        RenderUtils.glColorHex(color);

        int x = w / 2;
        int y = h / 2;

        Random rand = Minecraft.getInstance().world.rand;
        for (int i = 0; i < 5; i++) {
            int stipple = 0xFFFF & ~(3 << rand.nextInt(16));
            GL11.glLineStipple(4, (short) stipple);
            BufferBuilder bb = Tessellator.getInstance().getBuffer();
            float f = Minecraft.getInstance().gameSettings.mainHand == HandSide.RIGHT ? 0.665F : 0.335F;
            bb.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
            bb.pos(x + rand.nextInt(12) - 6, y + rand.nextInt(12) - 6, 0);
            bb.pos(w * f, h * 0.685F, 0);
            Tessellator.getInstance().draw();
//            GlStateManager.begin(GL11.GL_LINES);
//            GL11.glVertex2f(x + rand.nextInt(12) - 6, y + rand.nextInt(12) - 6);
//            GL11.glVertex2f(w * f, h * 0.685F);
//            GlStateManager.end();
        }
        RenderSystem.color4f(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }

    @SubscribeEvent
    public static void onWorldRender(RenderWorldLastEvent event) {
        // render everyone else's (and ours in 3rd person camera) minigun bullet traces
        PlayerEntity thisPlayer = Minecraft.getInstance().player;
//        double playerX = thisPlayer.prevPosX + (thisPlayer.getPosX() - thisPlayer.prevPosX) * event.getPartialTicks();
//        double playerY = thisPlayer.prevPosY + (thisPlayer.getPosY() - thisPlayer.prevPosY) * event.getPartialTicks();
//        double playerZ = thisPlayer.prevPosZ + (thisPlayer.getPosZ() - thisPlayer.prevPosZ) * event.getPartialTicks();

        MatrixStack matrixStack = event.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

        matrixStack.push();
        Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

//        GlStateManager.pushMatrix();
//        GlStateManager.translated(-playerX, -playerY, -playerZ);

        for (PlayerEntity player : Minecraft.getInstance().world.getPlayers()) {
            if (thisPlayer == player && Minecraft.getInstance().gameSettings.thirdPersonView == 0) continue;
            ItemStack curItem = player.getHeldItemMainhand();
            if (curItem.getItem() == ModItems.MINIGUN.get()) {
                Minigun minigun = ModItems.MINIGUN.get().getMinigun(curItem, player);
                if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                    matrixStack.push();
//                    GlStateManager.pushMatrix();
                    double playerX = player.prevPosX + (player.getPosX() - player.prevPosX) * event.getPartialTicks();
                    double playerY = player.prevPosY + (player.getPosY() - player.prevPosY) * event.getPartialTicks();
                    double playerZ = player.prevPosZ + (player.getPosZ() - player.prevPosZ) * event.getPartialTicks();
                    matrixStack.translate(playerX, playerY + 0.5, playerZ);
//                    GlStateManager.translated(playerX, playerY + 0.5, playerZ);
//                    GlStateManager.disableTexture();
//                    GlStateManager.enableBlend();
//                    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                    RenderUtils.glColorHex(0x40000000 | minigun.getAmmoColor());
                    Vec3d directionVec = player.getLookVec().normalize();
                    Vec3d vec = new Vec3d(directionVec.x, 0, directionVec.z).normalize();
                    vec.rotateYaw((float) Math.toRadians(-15 + (player.rotationYawHead - player.renderYawOffset)));
                    minigunFire.startX = vec.x * MINIGUN_RADIUS;
                    minigunFire.startY = vec.y * MINIGUN_RADIUS - player.getYOffset();
                    minigunFire.startZ = vec.z * MINIGUN_RADIUS;
                    for (int i = 0; i < 5; i++) {
                        minigunFire.endX = directionVec.x * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endY = directionVec.y * 20 + player.getEyeHeight() + player.getRNG().nextDouble() - 0.5;
                        minigunFire.endZ = directionVec.z * 20 + player.getRNG().nextDouble() - 0.5;
                        minigunFire.render(matrixStack, buffer.getBuffer(RenderType.LINES), 0x40000000 | minigun.getAmmoColor());
                    }
//                    GlStateManager.color4f(1, 1, 1, 1);
//                    GlStateManager.enableTexture();
//                    GlStateManager.popMatrix();
                    matrixStack.pop();
                }
            }
        }

        matrixStack.pop();
//        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void screenTilt(EntityViewRenderEvent.CameraSetup event) {
        if (event.getInfo().getRenderViewEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getInfo().getRenderViewEntity();
            if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.FEET) && !player.onGround) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                float targetRoll;
                float div = 50F;
                if (handler.isJetBootsActive() && !handler.isJetBootsBuilderMode()) {
                    float roll = player.rotationYawHead - player.prevRotationYawHead;
                    if (Math.abs(roll) < 0.0001) {
                        targetRoll = 0F;
                    } else {
                        targetRoll = Math.signum(roll) * MAX_SCREEN_ROLL;
                        div = Math.abs(400F / roll);
                    }
                } else {
                    targetRoll = 0F;
                }
                currentScreenRoll += (targetRoll - currentScreenRoll) / div;
                event.setRoll(currentScreenRoll);
            } else {
                currentScreenRoll = 0F;
            }
        }
    }

    @SubscribeEvent
    public static void jetBootsEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PlayerEntity player = event.player;
            if (player == null || player.world == null) return;
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            GameSettings settings = Minecraft.getInstance().gameSettings;
            if (handler.isJetBootsActive() && (!handler.isJetBootsEnabled() || !settings.keyBindJump.isKeyDown())) {
                NetworkHandler.sendToServer(new PacketJetBootsActivate(false));
                handler.setJetBootsActive(false);
            } else if (!handler.isJetBootsActive() && handler.isJetBootsEnabled() && settings.keyBindJump.isKeyDown()) {
                NetworkHandler.sendToServer(new PacketJetBootsActivate(true));
                handler.setJetBootsActive(true);
            }
        }
    }

//    private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);

    @SubscribeEvent
    public static void playerPreRotateEvent(RenderPlayerEvent.Pre event) {
        PlayerEntity player = event.getPlayer();
        JetBootsStateTracker tracker = JetBootsStateTracker.getTracker(player);
        JetBootsStateTracker.JetBootsState state = tracker.getJetBootsState(player);
        if (state != null && state.shouldRotatePlayer()) {
            MatrixStack matrixStack = event.getMatrixStack();
            matrixStack.push();
            matrixStack.rotate(makeQuaternion(player));
//            GlStateManager.pushMatrix();
//            GlStateManager.translated(event.getX(), event.getY(), event.getZ());
//            GlStateManager.multMatrix(quatToGlMatrix(makeQuaternion(player)));
//            GlStateManager.translated(-event.getX(), -event.getY(), -event.getZ());
            player.limbSwingAmount = player.prevLimbSwingAmount = 0F;
        }
    }

//    private static FloatBuffer quatToGlMatrix(Quaternion quaternionIn) {
//        // lifted from 1.12.2 GlStateManager
//        ClientEventHandler.BUF_FLOAT_16.clear();
//        float f = quaternionIn.getX() * quaternionIn.getX();
//        float f1 = quaternionIn.getX() * quaternionIn.getY();
//        float f2 = quaternionIn.getX() * quaternionIn.getZ();
//        float f3 = quaternionIn.getX() * quaternionIn.getW();
//        float f4 = quaternionIn.getY() * quaternionIn.getY();
//        float f5 = quaternionIn.getY() * quaternionIn.getZ();
//        float f6 = quaternionIn.getY() * quaternionIn.getW();
//        float f7 = quaternionIn.getZ() * quaternionIn.getZ();
//        float f8 = quaternionIn.getZ() * quaternionIn.getW();
//        ClientEventHandler.BUF_FLOAT_16.put(1.0F - 2.0F * (f4 + f7));
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f1 + f8));
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f2 - f6));
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f1 - f8));
//        ClientEventHandler.BUF_FLOAT_16.put(1.0F - 2.0F * (f + f7));
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f5 + f3));
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f2 + f6));
//        ClientEventHandler.BUF_FLOAT_16.put(2.0F * (f5 - f3));
//        ClientEventHandler.BUF_FLOAT_16.put(1.0F - 2.0F * (f + f4));
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(0.0F);
//        ClientEventHandler.BUF_FLOAT_16.put(1.0F);
//        ClientEventHandler.BUF_FLOAT_16.rewind();
//        return ClientEventHandler.BUF_FLOAT_16;
//    }

    @SubscribeEvent
    public static void playerPostRotateEvent(RenderPlayerEvent.Post event) {
        PlayerEntity player = event.getPlayer();
        JetBootsStateTracker tracker = JetBootsStateTracker.getTracker(player);
        JetBootsStateTracker.JetBootsState state = tracker.getJetBootsState(player);
        if (state != null && state.shouldRotatePlayer()) {
            event.getMatrixStack().pop();
//            GlStateManager.popMatrix();
        }
    }

    private static Quaternion makeQuaternion(PlayerEntity player) {
        Vec3d forward = player.getLookVec();

        double dot = new Vec3d(0, 1, 0).dotProduct(forward);
        if (Math.abs(dot + 1) < 0.000001) {
            return new Quaternion(0F, 1F, 0F, (float)Math.PI);
        }
        if (Math.abs (dot - 1) < 0.000001) {
            return new Quaternion(0, 0, 0, 1); //identity
        }

        Vec3d rotAxis = new Vec3d(0, 1, 0).crossProduct(forward).normalize();

        double a2 = Math.acos(dot) * .5f;
        float s = (float) Math.sin(a2);
        return new Quaternion((float) rotAxis.x * s, (float) rotAxis.y * s, (float) rotAxis.z * s, (float) Math.cos(a2));
    }

    @SubscribeEvent
    public static void adjustFOVEvent(FOVUpdateEvent event) {
        float modifier = 1.0f;
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            ItemStack stack = event.getEntity().getItemStackFromSlot(slot);
            if (stack.getItem() instanceof IFOVModifierItem) {
                modifier *= ((IFOVModifierItem) stack.getItem()).getFOVModifier(stack, event.getEntity(), slot);
            }
        }

        event.setNewfov(event.getNewfov() * modifier);
    }

    @SubscribeEvent
    public static void fogDensityEvent(EntityViewRenderEvent.FogDensity event) {
        if (event.getInfo().getFluidState().isTagged(FluidTags.WATER) && event.getInfo().getRenderViewEntity() instanceof PlayerEntity) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (handler.isArmorReady(EquipmentSlotType.HEAD) && handler.isScubaEnabled() && handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SCUBA) > 0) {
                event.setDensity(0.02f);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void guiContainerForeground(GuiContainerEvent.DrawForeground event) {
        if (Minecraft.getInstance().currentScreen instanceof IExtraGuiHandling) {
            ((IExtraGuiHandling) Minecraft.getInstance().currentScreen).drawExtras(event);
        }
    }

    @SubscribeEvent
    public static void drawCustomDurabilityBars(GuiScreenEvent.DrawScreenEvent.Pre event) {
        // with thanks to V0idWa1k3r
        // https://github.com/V0idWa1k3r/ExPetrum/blob/master/src/main/java/v0id/exp/client/ExPHandlerClient.java#L235
        if (event.getGui() instanceof ContainerScreen) {
            RenderSystem.disableTexture();
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            BufferBuilder bb = Tessellator.getInstance().getBuffer();
            ContainerScreen container = (ContainerScreen) event.getGui();
            int i = container.getGuiLeft();
            int j = container.getGuiTop();
            bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (Slot s : container.getContainer().inventorySlots) {
                if (s.getStack().getItem() instanceof ICustomDurabilityBar
                        && ((ICustomDurabilityBar) s.getStack().getItem()).shouldShowCustomDurabilityBar(s.getStack())) {
                    // render secondary durability bar
                    ICustomDurabilityBar custom = (ICustomDurabilityBar)s.getStack().getItem();
                    float x = s.xPos;
                    float y = s.yPos;
                    float val = custom.getCustomDurability(s.getStack());
                    int c = custom.getCustomDurabilityColour(s.getStack());
                    float r = ((c & 0xFF0000) >> 16) / 256f;
                    float g = ((c & 0xFF00) >> 8) / 256f;
                    float b = ((c & 0xFF)) / 256f;
                    int yOff = custom.shouldShowCustomDurabilityBar(s.getStack()) ? 0 : 1;

                    bb.pos(i + x + 2, j + y + 14 + yOff, 300).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                    bb.pos(i + x + 15, j + y + 14 + yOff, 300).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                    bb.pos(i + x + 15, j + y + 12 + yOff, 300).color(0.2F, 0.2F, 0.2F, 1F).endVertex();
                    bb.pos(i + x + 2, j + y + 12 + yOff, 300).color(0.2F, 0.2F, 0.2F, 1F).endVertex();

                    bb.pos(i + x + 2, j + y + 13 + yOff, 300).color(r, g, b, 1F).endVertex();
                    bb.pos(i + x + 2 + 13 * val, j + y + 13 + yOff, 300).color(r, g, b, 1F).endVertex();
                    bb.pos(i + x + 2 + 13 * val, j + y + 12 + yOff, 300).color(r, g, b, 1F).endVertex();
                    bb.pos(i + x + 2, j + y + 12 + yOff, 300).color(r, g, b, 1F).endVertex();

                }
            }
            Tessellator.getInstance().draw();
            RenderSystem.enableTexture();
        }
    }

    @SubscribeEvent
    public static void handleResolutionChange(GuiScreenEvent.InitGuiEvent event) {
        Screen gui = event.getGui();
        if (gui.getMinecraft().world != null) {
            MainWindow mw = gui.getMinecraft().getMainWindow();
            if (mw.getScaledWidth() != lastWidth || mw.getScaledHeight() != lastHeight) {
                HUDHandler.instance().onResolutionChanged();
                lastWidth = mw.getScaledWidth();
                lastHeight = mw.getScaledHeight();
            }
        }
    }

    public static Pair<Integer,Integer> getScaledScreenSize() {
        //noinspection SuspiciousNameCombination
        return Pair.of(lastWidth, lastHeight);
    }
}

