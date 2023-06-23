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

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.IKeyListener;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorColoringScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.overlays.PneumaticArmorHUDOverlay;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk.FeatureSetting;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.api.client.pneumatic_helmet.IClientArmorRegistry.DEFAULT_MESSAGE_BGCOLOR;
import static me.desht.pneumaticcraft.common.item.PneumaticArmorItem.isPneumaticArmorPiece;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Singleton object which manages all the Pneumatic Armor HUD drawing and input for the player.
 */
public enum HUDHandler implements IKeyListener {
    INSTANCE;

    private int lastScaledWidth = -1;
    private int lastScaledHeight = -1;

    private long lastArmorInitSound, lastArmorInitCompleteSound; // avoid too much sound spam when equipping armor
    private boolean sentForceInitPacket = false; // whether to send an armor init packet when helmet not equipped

    private final List<ArmorMessage> pendingMessages = new ArrayList<>();

    public static HUDHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Handles the 3D drawing for armor components (see {@link PneumaticArmorHUDOverlay} for 2D)
     */
    @SubscribeEvent
    public void renderHUD3d(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.options.hideGui || mc.screen != null
                || !PneumaticArmorItem.isPneumaticArmorPiece(player, EquipmentSlot.HEAD)
                || WidgetKeybindCheckBox.getCoreComponents() == null
                || !WidgetKeybindCheckBox.getCoreComponents().checked) {
            return;
        }

        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer(player);
        if (commonArmorHandler.getArmorPressure(EquipmentSlot.HEAD) > 0F) {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                if (commonArmorHandler.isArmorReady(slot)) {
                    List<IArmorUpgradeClientHandler<?>> clientHandlers = ClientArmorRegistry.getInstance().getHandlersForSlot(slot);
                    for (int i = 0; i < clientHandlers.size(); i++) {
                        if (commonArmorHandler.isUpgradeInserted(slot, i)
                                && WidgetKeybindCheckBox.forUpgrade(clientHandlers.get(i)).checked) {
                            clientHandlers.get(i).render3D(poseStack, buffer, event.getPartialTick());
                        }
                    }
                }
            }

            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            Player player = event.player;
            if (player == mc.player && player.level.isClientSide) {
                boolean anyArmorEquipped = false;
                CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer();
                for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    if (isPneumaticArmorPiece(player, slot)) {
                        tickArmorPiece(mc.player, slot, comHudHandler);
                        anyArmorEquipped = true;
                    }
                }
                if (anyArmorEquipped) {
                    ensureArmorInit(player, comHudHandler);
                    pendingMessages.forEach(ArmorMessage::tick);
                    pendingMessages.removeIf(message -> message == null || message.isExpired());
                } else {
                    pendingMessages.clear();
                    sentForceInitPacket = false;
                }
            }
        }
    }

    private void ensureArmorInit(Player player, CommonArmorHandler commonArmorHandler) {
        if (!isPneumaticArmorPiece(player, EquipmentSlot.HEAD) && !sentForceInitPacket) {
            // Special case: ensure core components packet always gets sent so armor can switch on even if helmet
            // is not equipped (core components is in the helmet for historical reasons)
            boolean state = WidgetKeybindCheckBox.getCoreComponents().checked;
            // core-components is always in slot HEAD, index 0
            if (state) {
                commonArmorHandler.setUpgradeEnabled(EquipmentSlot.HEAD, (byte) 0, true);
                NetworkHandler.sendToServer(new PacketToggleArmorFeature(EquipmentSlot.HEAD, (byte) 0, true));
            }
            sentForceInitPacket = true;
        }
    }

    private void tickArmorPiece(Player player, EquipmentSlot slot, CommonArmorHandler commonArmorHandler) {
        boolean armorEnabled = WidgetKeybindCheckBox.getCoreComponents().checked;
        List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        List<IArmorUpgradeClientHandler<?>> clientHandlers = ClientArmorRegistry.getInstance().getHandlersForSlot(slot);

        ItemStack armorStack = player.getItemBySlot(slot);
        int ticksSinceEquipped = commonArmorHandler.getTicksSinceEquipped(slot);
        int startupTime = commonArmorHandler.getStartupTime(slot);
        Component itemName = armorStack.getHoverName();

        if (ticksSinceEquipped > startupTime && armorEnabled) {
            // After full init: tick the client handler for each installed upgrade and open/close stat windows as needed
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (commonArmorHandler.isUpgradeInserted(slot, i)) {
                    IArmorUpgradeClientHandler<?> clientHandler = clientHandlers.get(i);
                    if (!clientHandler.isToggleable() || commonArmorHandler.isUpgradeEnabled(slot, i)) {
                        IGuiAnimatedStat stat = clientHandler.getAnimatedStat();
                        if (stat != null) {
                            IArmorUpgradeHandler<?> upgradeHandler = upgradeHandlers.get(i);
                            if (commonArmorHandler.getArmorPressure(slot) > upgradeHandler.getMinimumPressure()) {
                                stat.openStat();
                            } else {
                                stat.closeStat();
                            }
                            stat.tickWidget();
                        }
                    }
                    clientHandler.tickClient(commonArmorHandler, commonArmorHandler.isUpgradeEnabled(slot, i));
                }
            }
        } else if (ticksSinceEquipped == startupTime) {
            // full init: display "init complete" message
            playArmorInitCompleteSound(player);
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initComplete", itemName), 50, DEFAULT_MESSAGE_BGCOLOR));
            player.displayClientMessage(Component.translatable("pneumaticcraft.armor.message.configureHint", ClientUtils.translateKeyBind(KeyHandler.getInstance().keybindOpenOptions)).withStyle(ChatFormatting.YELLOW), true);
        } else if (ticksSinceEquipped == 0 && WidgetKeybindCheckBox.getCoreComponents().checked) {
            // tick 0: inform the server which upgrades are enabled
            for (IArmorUpgradeClientHandler<?> handler : ClientArmorRegistry.getInstance().getHandlersForSlot(slot)) {
                handler.reset();
            }
            List<FeatureSetting> features = new ArrayList<>();
            for (byte idx = 0; idx < upgradeHandlers.size(); idx++) {
                boolean state = armorEnabled && WidgetKeybindCheckBox.forUpgrade(upgradeHandlers.get(idx)).checked;
                commonArmorHandler.setUpgradeEnabled(slot, idx, state);
                features.add(new FeatureSetting(slot, idx, state));
            }
            NetworkHandler.sendToServer(new PacketToggleArmorFeatureBulk(features));
        } else if (ticksSinceEquipped == 1) {
            // tick 1: display the "init started" message
            playArmorInitSound(player, 0.5F);
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initStarted", itemName), 50, DEFAULT_MESSAGE_BGCOLOR));
        } else {
            // any other tick during startup: display found/not found message for each possible upgrade
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (ticksSinceEquipped == startupTime / (upgradeHandlers.size() + 2) * (i + 1)) {
                    playArmorInitSound(player, 0.5F + (float) (i + 1) / (upgradeHandlers.size() + 2) * 0.5F);
                    boolean upgradeEnabled = commonArmorHandler.isUpgradeInserted(slot, i);
                    Component message = xlate(upgradeHandlers.get(i).getTranslationKey())
                            .append(upgradeEnabled ? " installed" : " not installed");
                    addMessage(new ArmorMessage(message, 80, upgradeEnabled ? DEFAULT_MESSAGE_BGCOLOR : 0x70FF8000));
                }
            }
        }
    }

    private void playArmorInitSound(Player player, float pitch) {
        // avoid playing sounds too often... if many upgrades are installed it could get really noisy
        long when = player.level.getGameTime();
        if (when - lastArmorInitSound >= 30) {
            player.playNotifySound(ModSounds.HUD_INIT.get(), SoundSource.PLAYERS, 0.2F, pitch);
        }
        lastArmorInitSound = when;
    }

    private void playArmorInitCompleteSound(Player player) {
        long when = player.level.getGameTime();
        if (when - lastArmorInitCompleteSound >= 30) {
            player.playNotifySound(ModSounds.HUD_INIT_COMPLETE.get(), SoundSource.PLAYERS, 0.2F, (float) 1.0);
        }
        lastArmorInitCompleteSound = when;
    }

    public void addFeatureToggleMessage(String key, boolean enabled) {
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", xlate(key)),
                Collections.emptyList(), 60, DEFAULT_MESSAGE_BGCOLOR);
    }

    public void addFeatureToggleMessage(String key, String subKey, boolean enabled) {
        Component msg = xlate(key).append(": ").append(xlate(subKey));
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", msg),
                Collections.emptyList(), 60, DEFAULT_MESSAGE_BGCOLOR);
    }

    public void addMessage(Component title, List<Component> message, int duration, int backColor) {
        addMessage(new ArmorMessage(title, message, duration, backColor));
    }

    public void addMessage(ArmorMessage message) {
        if (pendingMessages.size() > 0) {
            // this message is dependent on the previous one, so renders right beneath it
            message.setDependingMessage(pendingMessages.get(pendingMessages.size() - 1).getStat());
        }
        pendingMessages.add(message);
    }

    @Override
    public void handleInput(KeyMapping key) {
        if (Minecraft.getInstance().isWindowActive()) {
            ClientArmorRegistry.getInstance().getTriggeredHandler(key)
                    .ifPresent(h -> h.onTriggered(CommonArmorHandler.getHandlerForPlayer()));
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        ClientArmorRegistry c = ClientArmorRegistry.getInstance();
        if (c.getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class).scroll(event)
                || c.getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class).scroll(event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void handleResolutionChange(ScreenEvent.Init event) {
        Screen gui = event.getScreen();
        if (gui.getMinecraft() != null && gui.getMinecraft().level != null) {
            Window mw = gui.getMinecraft().getWindow();
            if (mw.getGuiScaledWidth() != lastScaledWidth || mw.getGuiScaledHeight() != lastScaledHeight) {
                ClientArmorRegistry c = ClientArmorRegistry.getInstance();
                for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    c.getHandlersForSlot(slot).forEach(IArmorUpgradeClientHandler::onResolutionChanged);
                }
                lastScaledWidth = mw.getGuiScaledWidth();
                lastScaledHeight = mw.getGuiScaledHeight();
            }
        }
    }

    public int getStatOverlayColor() {
        // based on the eyepiece color but with the alpha locked to 3/16
        ItemStack stack = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlot.HEAD);
        int eyepieceColor = stack.getItem() instanceof PneumaticArmorItem helmet ?
                helmet.getEyepieceColor(stack) :
                ArmorColoringScreen.SelectorType.EYEPIECE.getDefaultColor();
        return (eyepieceColor & 0x00FFFFFF) | 0x30000000;
    }

    public void updateOverlayColors(EquipmentSlot slot) {
        int color = getStatOverlayColor();
        ClientArmorRegistry.getInstance().getHandlersForSlot(slot).forEach(clientHandler -> clientHandler.setOverlayColor(color));
    }

    public void renderMessages(PoseStack poseStack, float partialTicks) {
        pendingMessages.forEach(message -> message.renderMessage(poseStack, partialTicks));
    }
}
