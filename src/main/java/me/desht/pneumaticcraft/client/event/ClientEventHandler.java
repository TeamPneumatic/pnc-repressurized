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

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IFOVModifierItem;
import me.desht.pneumaticcraft.api.item.ICustomDurabilityBar;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.IExtraGuiHandling;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.IDrawAfterRender;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.config.subconfig.AuxConfigHandler;
import me.desht.pneumaticcraft.common.config.subconfig.IAuxConfig;
import me.desht.pneumaticcraft.common.item.IShiftScrollable;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketShiftScrollWheel;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.JetBootsStateTracker.JetBootsState;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler {
    private static float currentScreenRoll = 0F;

    @SubscribeEvent
    public static void screenTilt(ViewportEvent.ComputeCameraAngles event) {
        if (event.getCamera().getEntity() instanceof Player player) {
            if (PneumaticArmorItem.isPneumaticArmorPiece(player, EquipmentSlot.FEET) && !player.onGround()) {
                float targetRoll;
                float div = 50F;
                JetBootsState jbState = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
                if (jbState.isActive() && !jbState.isBuilderMode()) {
                    float roll = player.yHeadRot - player.yHeadRotO;
                    if (Math.abs(roll) < 0.0001) {
                        targetRoll = 0F;
                    } else {
                        targetRoll = Math.signum(roll) * ConfigHelper.client().armor.maxJetBootsFlightRoll.get();
                        div = Math.abs(100F / roll);
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
    public static void playerPreRotateEvent(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.isFallFlying()) {
            JetBootsState state = JetBootsStateTracker.getClientTracker().getJetBootsState(player);
            if (state != null && state.shouldRotatePlayer()) {
                player.walkAnimation.setSpeed(0f);
//                player.animationPosition = player.animationSpeed = 0F;
            }
        }
    }

    @SubscribeEvent
    public static void adjustFOVEvent(ComputeFovModifierEvent event) {
        float modifier = 1.0f;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = event.getPlayer().getItemBySlot(slot);
            if (stack.getItem() instanceof IFOVModifierItem fovModifier) {
                modifier *= fovModifier.getFOVModifier(stack, event.getPlayer(), slot);
            }
        }

        event.setNewFovModifier(event.getNewFovModifier() * modifier);
    }

    @SubscribeEvent
    public static void fogDensityEvent(ViewportEvent.RenderFog event) {
        if (event.getCamera().getFluidInCamera() == FogType.WATER && event.getCamera().getEntity() instanceof Player) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (handler.upgradeUsable(CommonUpgradeHandlers.scubaHandler, true)) {
                event.setNearPlaneDistance(20f);
                event.setNearPlaneDistance(50f);
                event.setFogShape(FogShape.SPHERE);
//                event.setDensity(350f);
                event.setCanceled(true);
            }
        }
    }

    private static final int Z_LEVEL = 233;  // should be just above the drawn itemstack

    @SubscribeEvent
    public static void guiContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        // general extra rendering
        if (event.getContainerScreen() instanceof IExtraGuiHandling e) {
            e.drawExtras(event);
        }

        // custom durability bars
//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        AbstractContainerScreen<?> container = event.getContainerScreen();
        PoseStack poseStack = event.getGuiGraphics().pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        for (Slot s : container.getMenu().slots) {
            if (s.getItem().getItem() instanceof ICustomDurabilityBar custom) {
                if (custom.shouldShowCustomDurabilityBar(s.getItem())) {
                    int x = s.x;
                    int y = s.y;
                    int width = Math.round(custom.getCustomDurability(s.getItem()) * 13);
                    int col = custom.getCustomDurabilityColour(s.getItem());
                    int yOff = custom.isShowingOtherBar(s.getItem()) ? 0 : 1;
                    if (yOff == 1) {
                        event.getGuiGraphics().fill(x + 2, y + 14, x + 2 + width, y + 15, 0xFF282828);
                    }
                    event.getGuiGraphics().fill(x + 2, y + 12 + yOff, x + 15, y + 13 + yOff, 0xFF000000);
                    event.getGuiGraphics().fill(x + 2, y + 12 + yOff, x + 2 + width, y + 13 + yOff, 0xFF000000 | col);
                }
            }
        }
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onGuiDrawPost(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractPneumaticCraftContainerScreen || event.getScreen() instanceof AbstractPneumaticCraftScreen) {
            List<IDrawAfterRender> toDraw = event.getScreen().children().stream()
                    .filter(l -> l instanceof IDrawAfterRender)
                    .map(l -> (IDrawAfterRender) l)
                    .toList();
            if (!toDraw.isEmpty()) {
                PoseStack poseStack = event.getGuiGraphics().pose();
                poseStack.pushPose();
                poseStack.translate(0, 0, 500);
                toDraw.forEach(d -> d.renderAfterEverythingElse(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick()));
                poseStack.popPose();
            }
        }
    }

    @SubscribeEvent
    public static void onShiftScroll(InputEvent.MouseScrollingEvent event) {
        if (ClientUtils.getClientPlayer().isCrouching()) {
            if (!tryHand(event, InteractionHand.MAIN_HAND)) tryHand(event, InteractionHand.OFF_HAND);
        }
    }

    private static boolean tryHand(InputEvent.MouseScrollingEvent event, InteractionHand hand) {
        ItemStack stack = ClientUtils.getClientPlayer().getItemInHand(hand);
        if (stack.getItem() instanceof IShiftScrollable s) {
            NetworkHandler.sendToServer(new PacketShiftScrollWheel(event.getScrollDelta() > 0, InteractionHand.MAIN_HAND));
            s.onShiftScrolled(ClientUtils.getClientPlayer(), event.getScrollDelta() > 0, InteractionHand.MAIN_HAND);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onClientConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        AuxConfigHandler.postInit(IAuxConfig.Sidedness.CLIENT);
        ArmorMainScreen.initHelmetCoreComponents();
    }
}

