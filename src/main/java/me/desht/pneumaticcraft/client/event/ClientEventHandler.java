/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.IExtraGuiHandling;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.IDrawAfterRender;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.event.DateEventHandler;
import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketJetBootsActivate;
import me.desht.pneumaticcraft.common.network.PacketShiftScrollWheel;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker.JetBootsState;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.JetBootsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {
    private static final float MAX_SCREEN_ROLL = 25F;  // max roll in degrees when flying with jetboots

    private static float currentScreenRoll = 0F;

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Pre<?,?> event) {
        setRenderHead(event.getEntity(), false);
    }

    @SubscribeEvent
    public static void onLivingRender(RenderLivingEvent.Post<?,?> event) {
        setRenderHead(event.getEntity(), true);
    }

    private static void setRenderHead(LivingEntity entity, boolean setRender) {
        if (entity.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.PNEUMATIC_HELMET.get()
                && (ConfigHelper.client().armor.fancyArmorModels.get() || DateEventHandler.isIronManEvent())) {
            EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof HumanoidMobRenderer) {
                HumanoidModel<?> modelBiped = ((HumanoidMobRenderer<?,?>) renderer).getModel();
                modelBiped.head.visible = setRender;
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

//    @SubscribeEvent
//    public static void tickEnd(TickEvent.RenderTickEvent event) {
//        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().isGameFocused()
//                && ClientUtils.getClientPlayer() != null
//                && (ModuleRegulatorTube.inverted || !ModuleRegulatorTube.inLine)) {
//            Minecraft mc = Minecraft.getInstance();
//            MainWindow mw = mc.getMainWindow();
//            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
//            String warning = TextFormatting.RED + I18n.format("pneumaticcraft.gui.regulatorTube.hudMessage." + (ModuleRegulatorTube.inverted ? "inverted" : "notInLine"));
//            fontRenderer.drawStringWithShadow(warning, mw.getScaledWidth() / 2f - fontRenderer.getStringWidth(warning) / 2f, mw.getScaledHeight() / 2f + 30, 0xFFFFFFFF);
//        }
//    }

    @SubscribeEvent
    public static void renderThirdPersonMinigunTraces(RenderPlayerEvent.Post event) {
        // render everyone else's (and ours, in 3rd person camera) minigun bullet traces
        Player player = event.getPlayer();
        if (player == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        ItemStack curItem = player.getMainHandItem();
        if (curItem.getItem() == ModItems.MINIGUN.get()) {
            Minigun minigun = ModItems.MINIGUN.get().getMinigun(curItem, player);
            if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
                VertexConsumer builder = event.getMultiBufferSource().getBuffer(RenderType.LINES);
                // FIXME: this just doesn't place the start of the line where it should... why?
                Vec3 startVec = new Vec3(0, player.getEyeHeight() / 2, 0).add(player.getLookAngle());
                Vec3 endVec = startVec.add(player.getLookAngle().scale(20));
                int[] cols = RenderUtils.decomposeColor(minigun.getAmmoColor());
                Matrix4f posMat = event.getPoseStack().last().pose();
                for (int i = 0; i < 5; i++) {
                    RenderUtils.posF(builder, posMat, startVec.x, startVec.y, startVec.z)
                            .color(cols[1], cols[2], cols[3], 64)
                            .endVertex();
                    RenderUtils.posF(builder, posMat, endVec.x + player.getRandom().nextDouble() - 0.5, endVec.y + player.getRandom().nextDouble() - 0.5, endVec.z + player.getRandom().nextDouble() - 0.5)
                            .color(cols[1], cols[2], cols[3], 64)
                            .endVertex();
                }
            }
        }
    }

    @SubscribeEvent
    public static void screenTilt(EntityViewRenderEvent.CameraSetup event) {
        if (event.getCamera().getEntity() instanceof Player player) {
            if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlot.FEET) && !player.isOnGround()) {
                float targetRoll;
                float div = 50F;
                JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
                if (jbState.isActive() && !jbState.isBuilderMode()) {
                    float roll = player.yHeadRot - player.yHeadRotO;
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
            Player player = event.player;
            if (player == null || player.level == null || !player.level.isClientSide) return;
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            JetBootsHandler jbHandler = ArmorUpgradeRegistry.getInstance().jetBootsHandler;
            JetBootsState jbState = jbHandler.getJetBootsSyncedState(handler);
            if (handler.upgradeUsable(jbHandler, false)) {
                if (jbState.isActive() && (!jbState.isEnabled() || !thrustKeyPressed(jbState.isBuilderMode()))) {
                    NetworkHandler.sendToServer(new PacketJetBootsActivate(false));
                    jbHandler.setJetBootsActive(handler, false);
                } else if (!jbState.isActive() && jbState.isEnabled() && thrustKeyPressed(jbState.isBuilderMode())) {
                    NetworkHandler.sendToServer(new PacketJetBootsActivate(true));
                    jbHandler.setJetBootsActive(handler, true);
                }
            }
        }
    }

    private static boolean thrustKeyPressed(boolean builderMode) {
        return KeyHandler.getInstance().keybindJetBoots.isDown() || builderMode && Minecraft.getInstance().options.keyJump.isDown();
    }

    @SubscribeEvent
    public static void playerPreRotateEvent(RenderPlayerEvent.Pre event) {
        Player player = event.getPlayer();
        if (!player.isFallFlying()) {
            JetBootsState state = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
            if (state != null && state.shouldRotatePlayer()) {
                player.animationPosition = player.animationSpeed = 0F;
            }
        }
    }

    @SubscribeEvent
    public static void adjustFOVEvent(FOVModifierEvent event) {
        float modifier = 1.0f;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = event.getEntity().getItemBySlot(slot);
            if (stack.getItem() instanceof IFOVModifierItem) {
                modifier *= ((IFOVModifierItem) stack.getItem()).getFOVModifier(stack, event.getEntity(), slot);
            }
        }

        event.setNewfov(event.getNewfov() * modifier);
    }

    @SubscribeEvent
    public static void fogDensityEvent(EntityViewRenderEvent.FogDensity event) {
        if (event.getCamera().getFluidInCamera() == FogType.WATER && event.getCamera().getEntity() instanceof Player) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().scubaHandler, true)) {
                event.setDensity(0.02f);
                event.setCanceled(true);
            }
        }
    }

    private static final int Z_LEVEL = 233;  // should be just above the drawn itemstack

    @SubscribeEvent
    public static void guiContainerForeground(ContainerScreenEvent.DrawForeground event) {
        // general extra rendering
        if (Minecraft.getInstance().screen instanceof IExtraGuiHandling) {
            ((IExtraGuiHandling) Minecraft.getInstance().screen).drawExtras(event);
        }

        // custom durability bars
        RenderSystem.disableTexture();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        AbstractContainerScreen<?> container = event.getContainerScreen();
        PoseStack matrixStack = event.getPoseStack();
        for (Slot s : container.getMenu().slots) {
            if (s.getItem().getItem() instanceof ICustomDurabilityBar) {
                ICustomDurabilityBar custom = (ICustomDurabilityBar) s.getItem().getItem();
                if (custom.shouldShowCustomDurabilityBar(s.getItem())) {
                    int x = s.x;
                    int y = s.y;
                    float width = custom.getCustomDurability(s.getItem()) * 13;
                    int[] cols = RenderUtils.decomposeColor(custom.getCustomDurabilityColour(s.getItem()));
                    int yOff = custom.isShowingOtherBar(s.getItem()) ? 0 : 1;
                    if (yOff == 1) {
                        GuiUtils.drawUntexturedQuad(matrixStack, bb, x + 2, y + 14, Z_LEVEL, width, 1, 40, 40, 40, 255);
                    }
                    GuiUtils.drawUntexturedQuad(matrixStack, bb, x + 2, y + 12 + yOff, Z_LEVEL, 13, 1, 0, 0, 0, 255);
                    GuiUtils.drawUntexturedQuad(matrixStack, bb, x + 2, y + 12 + yOff, Z_LEVEL, width, 1, cols[1], cols[2], cols[3], 255);
                }
            }
        }
        RenderSystem.enableTexture();
    }

    @SubscribeEvent
    public static void onGuiDrawPost(ScreenEvent.DrawScreenEvent.Post event) {
        if (event.getScreen() instanceof GuiPneumaticContainerBase || event.getScreen() instanceof GuiPneumaticScreenBase) {
            for (GuiEventListener l : event.getScreen().children()) {
                if (l instanceof IDrawAfterRender) {
                    ((IDrawAfterRender) l).renderAfterEverythingElse(event.getPoseStack(), event.getMouseX(), event.getMouseY(), event.getPartialTicks());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onShiftScroll(InputEvent.MouseScrollEvent event) {
        if (ClientUtils.getClientPlayer().isCrouching()) {
            if (!tryHand(event, InteractionHand.MAIN_HAND)) tryHand(event, InteractionHand.OFF_HAND);
        }
    }

    private static boolean tryHand(InputEvent.MouseScrollEvent event, InteractionHand hand) {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
        if (stack.getItem() instanceof IShiftScrollable) {
            NetworkHandler.sendToServer(new PacketShiftScrollWheel(event.getScrollDelta() > 0, InteractionHand.MAIN_HAND));
            ((IShiftScrollable) stack.getItem()).onShiftScrolled(ClientUtils.getClientPlayer(), event.getScrollDelta() > 0, InteractionHand.MAIN_HAND);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
        GuiArmorMainScreen.initHelmetCoreComponents();
    }
}

