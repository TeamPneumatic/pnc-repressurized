package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.IKeyListener;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorColors;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeatureBulk.FeatureSetting;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.item.ItemPneumaticArmor.isPneumaticArmorPiece;
import static me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler.CRITICAL_PRESSURE;
import static me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler.LOW_PRESSURE;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Singleton object which manages all the Pneumatic Armor HUD drawing and input for the player.
 */
public enum HUDHandler implements IKeyListener {
    INSTANCE;

    private static final int PROGRESS_BAR_HEIGHT = 17;

    private int lastScaledWidth = -1;
    private int lastScaledHeight = -1;

    private long lastArmorInitSound, lastArmorInitCompleteSound; // avoid too much sound spam when equipping armor
    private boolean sentForceInitPacket = false; // whether to send an armor init packet when helmet not equipped

    private final List<ArmorMessage> pendingMessages = new ArrayList<>();
    private final boolean[] gaveCriticalWarning = new boolean[4];  // per-slot
    private final boolean[] gaveLowPressureWarning = new boolean[4];  // per-slot

    public static HUDHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Handles the 3D drawing for armor components
     */
    @SubscribeEvent
    public void renderHUD3d(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        if (player == null || mc.options.hideGui || mc.screen != null
                || !ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)
                || WidgetKeybindCheckBox.getCoreComponents() == null
                || !WidgetKeybindCheckBox.getCoreComponents().checked) {
            return;
        }

        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer(player);
        if (commonArmorHandler.getArmorPressure(EquipmentSlotType.HEAD) > 0F) {
            IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            MatrixStack matrixStack = event.getMatrixStack();
            matrixStack.pushPose();

            Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                if (commonArmorHandler.isArmorReady(slot)) {
                    GlStateManager._disableTexture();
                    List<IArmorUpgradeClientHandler<?>> clientHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);
                    for (int i = 0; i < clientHandlers.size(); i++) {
                        if (commonArmorHandler.isUpgradeInserted(slot, i)
                                && WidgetKeybindCheckBox.forUpgrade(clientHandlers.get(i)).checked) {
                            clientHandlers.get(i).render3D(matrixStack, buffer, event.getPartialTicks());
                        }
                    }
                    GlStateManager._enableTexture();
                }
            }

            matrixStack.popPose();
        }
    }

    /**
     * Handles the 2D overlay drawing for the armor components
     */
    @SubscribeEvent
    public void renderHUD2d(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) return;

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        if (player == null || mc.options.hideGui || mc.screen != null
                || !ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(player)) {
            return;
        }

        float partialTicks = event.getPartialTicks();

        MatrixStack matrixStack = event.getMatrixStack();

        MainWindow mw = event.getWindow();
        matrixStack.pushPose();
        RenderSystem.color4f(0, 1, 0, 0.8F);
        CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer(player);

        boolean anyArmorInInit = false;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (armorStack.getItem() instanceof ItemPneumaticArmor && comHudHandler.getArmorPressure(slot) >= 0.0001f && !comHudHandler.isArmorReady(slot)) {
                anyArmorInInit = true;
                break;
            }
        }

        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (!(armorStack.getItem() instanceof ItemPneumaticArmor) || comHudHandler.getArmorPressure(slot) < 0.0001F) {
                continue;
            }
            if (anyArmorInInit) {
                // draw initialization progress bar(s)
                if (comHudHandler.isArmorEnabled()) {
                    int xLeft = mw.getGuiScaledWidth() / 2;
                    int yOffset = 10 + (3 - slot.getIndex()) * PROGRESS_BAR_HEIGHT;
                    float progress = comHudHandler.getTicksSinceEquipped(slot) * 100f / comHudHandler.getStartupTime(slot);
                    progress = Math.min(100, progress + event.getPartialTicks());
                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderProgressBar.render2d(matrixStack,mw.getGuiScaledWidth() / 2f, yOffset,
                            mw.getGuiScaledWidth() - 10, yOffset + PROGRESS_BAR_HEIGHT - 1, -90F,
                            progress,0xAAFFC000, 0xAA00FF00);
                    RenderSystem.disableBlend();
                    RenderSystem.enableTexture();
                    GuiUtils.renderItemStack(matrixStack, armorStack,xLeft + 2, yOffset);
                }
            }
            if (comHudHandler.isArmorReady(slot)) {
                float pressure = comHudHandler.getArmorPressure(slot);

                handlePressureWarnings(player, slot, pressure);

                // all enabled upgrades do their 2D rendering here
                if (WidgetKeybindCheckBox.getCoreComponents().checked || Minecraft.getInstance().screen == null) {
                    List<IArmorUpgradeClientHandler<?>> renderHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        IArmorUpgradeClientHandler<?> clientHandler = renderHandlers.get(i);
                        if (comHudHandler.isUpgradeInserted(slot, i) && (comHudHandler.isUpgradeEnabled(slot, i) || !clientHandler.isToggleable())) {
                            IGuiAnimatedStat stat = clientHandler.getAnimatedStat();
                            if (stat != null) {
                                stat.renderStat(matrixStack, -1, -1, partialTicks);
                            }
                            clientHandler.render2D(matrixStack, partialTicks, pressure > 0F);
                        }
                    }
                }
            }
        }

        // render every pending message
        pendingMessages.forEach(message -> message.renderMessage(matrixStack, partialTicks));

        matrixStack.popPose();

        RenderSystem.color4f(1, 1, 1, 1);

        // show armor initialisation percentages
        if (comHudHandler.isArmorEnabled() && anyArmorInInit) {
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                if (isPneumaticArmorPiece(player, slot) && comHudHandler.getArmorPressure(slot) > 0F) {
                    String text = Math.min(100, comHudHandler.getTicksSinceEquipped(slot) * 100 / comHudHandler.getStartupTime(slot)) + "%";
                    mc.font.drawShadow(matrixStack, text, mw.getGuiScaledWidth() * 0.75f - 8, 14 + PROGRESS_BAR_HEIGHT * (3 - slot.getIndex()), 0xFFFF40);
                }
            }
        }

        RenderSystem.enableBlend(); // without this, nether portal overlay rendering will be broken
    }

    private void handlePressureWarnings(PlayerEntity player, EquipmentSlotType slot, float pressure) {
        // low/no pressure warnings
        if (pressure <= CRITICAL_PRESSURE && !gaveCriticalWarning[slot.getIndex()]) {
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.outOfAir", player.getItemBySlot(slot).getHoverName()),
                    100, 0x70FF0000));
            gaveCriticalWarning[slot.getIndex()] = true;
            player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 2f);
        } else if (pressure > CRITICAL_PRESSURE && pressure <= LOW_PRESSURE && !gaveLowPressureWarning[slot.getIndex()]) {
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.almostOutOfAir", player.getItemBySlot(slot).getHoverName()),
                    60, 0x70FF8000));
            gaveLowPressureWarning[slot.getIndex()] = true;
        }

        if (pressure > LOW_PRESSURE + 0.1F) gaveLowPressureWarning[slot.getIndex()] = false;
        if (pressure > CRITICAL_PRESSURE + 0.1F) gaveCriticalWarning[slot.getIndex()] = false;
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = event.player;
            if (player == mc.player && player.level.isClientSide) {
                boolean anyArmorEquipped = false;
                CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer();
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    if (isPneumaticArmorPiece(player, slot)) {
                        tickArmorPiece(mc.player, slot, comHudHandler);
                        anyArmorEquipped = true;
                    }
                }
                if (anyArmorEquipped) {
                    ensureArmorInit(player, comHudHandler);
                    pendingMessages.forEach(message -> message.getStat().tickWidget());
                    pendingMessages.removeIf(message -> message == null || --message.lifeSpan <= 0);
                } else {
                    pendingMessages.clear();
                    sentForceInitPacket = false;
                }
            }
        }
    }

    private void ensureArmorInit(PlayerEntity player, CommonArmorHandler commonArmorHandler) {
        if (!isPneumaticArmorPiece(player, EquipmentSlotType.HEAD) && !sentForceInitPacket) {
            // Special case: ensure core components packet always gets sent so armor can switch on even if helmet
            // is not equipped (core components is in the helmet for historical reasons)
            boolean state = WidgetKeybindCheckBox.getCoreComponents().checked;
            // core-components is always in slot HEAD, index 0
            if (state) {
                commonArmorHandler.setUpgradeEnabled(EquipmentSlotType.HEAD, (byte) 0, true);
                NetworkHandler.sendToServer(new PacketToggleArmorFeature(EquipmentSlotType.HEAD, (byte) 0, true));
            }
            sentForceInitPacket = true;
        }
    }

    private void tickArmorPiece(PlayerEntity player, EquipmentSlotType slot, CommonArmorHandler commonArmorHandler) {
        boolean armorEnabled = WidgetKeybindCheckBox.getCoreComponents().checked;
        List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
        List<IArmorUpgradeClientHandler<?>> clientHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);

        ItemStack armorStack = player.getItemBySlot(slot);
        int ticksSinceEquipped = commonArmorHandler.getTicksSinceEquipped(slot);
        int startupTime = commonArmorHandler.getStartupTime(slot);
        ITextComponent itemName = armorStack.getHoverName();

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
                        clientHandler.tickClient(commonArmorHandler);
                    }
                }
            }
        } else if (ticksSinceEquipped == startupTime) {
            // full init: display "init complete" message
            playArmorInitCompleteSound(player);
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initComplete", itemName), 50, 0x7000AA00));
        } else if (ticksSinceEquipped == 0 && WidgetKeybindCheckBox.getCoreComponents().checked) {
            // tick 0: inform the server which upgrades are enabled
            for (IArmorUpgradeClientHandler<?> handler : ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot)) {
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
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initStarted", itemName), 50, 0x7000AA00));
        } else {
            // any other tick during startup: display found/not found message for each possible upgrade
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (ticksSinceEquipped == startupTime / (upgradeHandlers.size() + 2) * (i + 1)) {
                    playArmorInitSound(player, 0.5F + (float) (i + 1) / (upgradeHandlers.size() + 2) * 0.5F);
                    boolean upgradeEnabled = commonArmorHandler.isUpgradeInserted(slot, i);
                    ITextComponent message = xlate(upgradeHandlers.get(i).getTranslationKey())
                            .append(upgradeEnabled ? " installed" : " not installed");
                    addMessage(new ArmorMessage(message, 80, upgradeEnabled ? 0x7000AA00 : 0x70FF8000));
                }
            }
        }
    }

    private void playArmorInitSound(PlayerEntity player, float pitch) {
        // avoid playing sounds too often... if many upgrades are installed it could get really noisy
        long when = player.level.getGameTime();
        if (when - lastArmorInitSound >= 30) {
            player.playNotifySound(ModSounds.HUD_INIT.get(), SoundCategory.PLAYERS, 0.2F, pitch);
        }
        lastArmorInitSound = when;
    }

    private void playArmorInitCompleteSound(PlayerEntity player) {
        long when = player.level.getGameTime();
        if (when - lastArmorInitCompleteSound >= 30) {
            player.playNotifySound(ModSounds.HUD_INIT_COMPLETE.get(), SoundCategory.PLAYERS, 0.2F, (float) 1.0);
        }
        lastArmorInitCompleteSound = when;
    }

    public void addFeatureToggleMessage(String key, boolean enabled) {
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", xlate(key)),
                Collections.emptyList(), 60, 0x7000AA00);
    }

    public void addFeatureToggleMessage(String key, String subKey, boolean enabled) {
        ITextComponent msg = xlate(key).append(": ").append(xlate(subKey));
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", msg),
                Collections.emptyList(), 60, 0x7000AA00);
    }

    public void addMessage(ITextComponent title, List<ITextComponent> message, int duration, int backColor) {
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
    public void handleInput(KeyBinding key) {
        if (Minecraft.getInstance().isWindowActive()) {
            ArmorUpgradeClientRegistry.getInstance().getTriggeredHandler(key)
                    .ifPresent(h -> h.onTriggered(CommonArmorHandler.getHandlerForPlayer()));
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollEvent event) {
        ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();
        ArmorUpgradeClientRegistry c = ArmorUpgradeClientRegistry.getInstance();
        boolean isCaptured = c.getClientHandler(r.blockTrackerHandler, BlockTrackerClientHandler.class).scroll(event);
        if (!isCaptured) isCaptured = c.getClientHandler(r.entityTrackerHandler, EntityTrackerClientHandler.class).scroll(event);
        if (isCaptured) event.setCanceled(true);
    }

    @SubscribeEvent
    public void handleResolutionChange(GuiScreenEvent.InitGuiEvent event) {
        Screen gui = event.getGui();
        if (gui.getMinecraft().level != null) {
            MainWindow mw = gui.getMinecraft().getWindow();
            if (mw.getGuiScaledWidth() != lastScaledWidth || mw.getGuiScaledHeight() != lastScaledHeight) {
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    for (IArmorUpgradeClientHandler<?> handler : ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot)) {
                        handler.onResolutionChanged();
                    }
                }
                lastScaledWidth = mw.getGuiScaledWidth();
                lastScaledHeight = mw.getGuiScaledHeight();
            }
        }
    }

    public int getStatOverlayColor() {
        // based on the eyepiece color but with the alpha locked to 3/16
        ItemStack stack = Minecraft.getInstance().player.getItemBySlot(EquipmentSlotType.HEAD);
        int eyepieceColor = stack.getItem() instanceof ItemPneumaticArmor ?
                ((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack) :
                GuiArmorColors.SelectorType.EYEPIECE.getDefaultColor();
        return (eyepieceColor & 0x00FFFFFF) | 0x30000000;
    }

    public void updateOverlayColors(EquipmentSlotType slot) {
        int color = getStatOverlayColor();
        ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot).forEach(clientHandler -> clientHandler.setOverlayColor(color));
    }
}
