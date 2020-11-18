package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.IKeyListener;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackClientHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPneumaticKick;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
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
import net.minecraft.util.SoundEvent;
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
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * Singleton object which manages all the Pneumatic Armor HUD drawing for the player.
 */
public enum HUDHandler implements IKeyListener {
    INSTANCE;

    private static final int PROGRESS_BAR_HEIGHT = 17;

    private int lastScaledWidth = -1;
    private int lastScaledHeight = -1;

    private long lastArmorInitSound; // avoid too much sound spam when equipping armor
    private boolean sentForceInitPacket = false; // whether to send an armor init packet when helmet not equipped

    private final List<ArmorMessage> pendingMessages = new ArrayList<>();
    private final boolean[] gaveEmptyWarning = new boolean[4];  // per-slot
    private final boolean[] gaveNearlyEmptyWarning = new boolean[4];  // per-slot

    public static HUDHandler getInstance() {
        return INSTANCE;
    }

    public <T extends IArmorUpgradeClientHandler> T getSpecificRenderer(Class<T> clazz) {
        return ArmorUpgradeClientRegistry.getInstance().byClass(clazz);
    }

    /**
     * Handles the 3D drawing for armor components
     */
    @SubscribeEvent
    public void renderHUD3d(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!WidgetKeybindCheckBox.getCoreComponents().checked || mc.gameSettings.hideGUI) return;

        PlayerEntity player = mc.player;
        if (player == null) return;

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        MatrixStack matrixStack = event.getMatrixStack();

        matrixStack.push();

        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        ItemStack helmetStack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer(player);
        if (helmetStack.getItem() instanceof ItemPneumaticArmor && commonArmorHandler.getArmorPressure(EquipmentSlotType.HEAD) > 0F) {
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                if (commonArmorHandler.isArmorReady(slot)) {
                    GlStateManager.disableTexture();
                    List<IArmorUpgradeClientHandler> clientHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);
                    for (int i = 0; i < clientHandlers.size(); i++) {
                        if (commonArmorHandler.isUpgradeInserted(slot, i)
                                && WidgetKeybindCheckBox.forUpgrade(clientHandlers.get(i)).checked) {
                            clientHandlers.get(i).render3D(matrixStack, buffer, event.getPartialTicks());
                        }
                    }
                    GlStateManager.enableTexture();
                }
            }
        }

        matrixStack.pop();
    }

    /**
     * Handles the 2D overlay drawing for the armor components
     */
    @SubscribeEvent
    public void renderHUD2d(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HELMET) return;

        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;

        if (mc.gameSettings.hideGUI || player == null || mc.currentScreen != null || !ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(player)) {
            return;
        }

        float partialTicks = event.getPartialTicks();

        MatrixStack matrixStack = event.getMatrixStack();

        MainWindow mw = event.getWindow();
        matrixStack.push();
        RenderSystem.color4f(0, 1, 0, 0.8F);
        CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer(player);

        boolean anyArmorInInit = false;
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (armorStack.getItem() instanceof ItemPneumaticArmor && !comHudHandler.isArmorReady(slot)) {
                anyArmorInInit = true;
                break;
            }
        }

        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemStackFromSlot(slot);
            if (!(armorStack.getItem() instanceof ItemPneumaticArmor) || comHudHandler.getArmorPressure(slot) < 0.0001F) {
                continue;
            }
            if (anyArmorInInit) {
                // draw initialization progress bar(s)
                gaveEmptyWarning[slot.getIndex()] = false;
                gaveNearlyEmptyWarning[slot.getIndex()] = false;
                if (comHudHandler.isArmorEnabled()) {
                    int xLeft = mw.getScaledWidth() / 2;
                    int yOffset = 10 + (3 - slot.getIndex()) * PROGRESS_BAR_HEIGHT;
                    float progress = comHudHandler.getTicksSinceEquipped(slot) * 100f / comHudHandler.getStartupTime(slot);
                    progress = Math.min(100, progress + event.getPartialTicks());
                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    RenderProgressBar.render2d(matrixStack,mw.getScaledWidth() / 2f, yOffset,
                            mw.getScaledWidth() - 10, yOffset + PROGRESS_BAR_HEIGHT - 1, -90F,
                            progress,0xAAFFC000, 0xAA00FF00);
                    RenderSystem.disableBlend();
                    RenderSystem.enableTexture();
                    GuiUtils.renderItemStack(matrixStack, armorStack,xLeft + 2, yOffset);
                }
            }
            if (comHudHandler.isArmorReady(slot)) {
                ITextComponent itemName = armorStack.getDisplayName();
                float pressure = comHudHandler.getArmorPressure(slot);

                // low/no pressure warnings
                if (pressure < 0.05F && !gaveEmptyWarning[slot.getIndex()]) {
                    addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.outOfAir", itemName), 100, 0x70FF0000));
                    gaveEmptyWarning[slot.getIndex()] = true;
                }
                if (pressure > 0.2F && pressure < 0.5F && !gaveNearlyEmptyWarning[slot.getIndex()]) {
                    addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.almostOutOfAir", itemName), 60, 0x70FF8000));
                    gaveNearlyEmptyWarning[slot.getIndex()] = true;
                }

                // all enabled upgrades do their 2D rendering here
                if (WidgetKeybindCheckBox.getCoreComponents().checked || Minecraft.getInstance().currentScreen == null) {
                    List<IArmorUpgradeClientHandler> renderHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        IArmorUpgradeClientHandler clientHandler = renderHandlers.get(i);
                        if (comHudHandler.isUpgradeInserted(slot, i) && comHudHandler.isUpgradeEnabled(slot, i)) {
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

        // chestplate launcher upgrade (if installed)
        if (LauncherTracker.INSTANCE.getLauncherProgress() > 0) {
            LauncherTracker.INSTANCE.render(matrixStack, mw, partialTicks);
        }

        // render every pending message
        pendingMessages.forEach(message -> message.renderMessage(matrixStack, partialTicks));

        RenderSystem.disableBlend();
        matrixStack.pop();

        RenderSystem.color4f(1, 1, 1, 1);

        // show armor initialisation percentages
        if (comHudHandler.isArmorEnabled() && anyArmorInInit) {
            for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                if (isPneumaticArmorPiece(player, slot) && comHudHandler.getArmorPressure(slot) > 0F) {
                    String text = Math.min(100, comHudHandler.getTicksSinceEquipped(slot) * 100 / comHudHandler.getStartupTime(slot)) + "%";
                    mc.fontRenderer.drawStringWithShadow(matrixStack, text, mw.getScaledWidth() * 0.75f - 8, 14 + PROGRESS_BAR_HEIGHT * (3 - slot.getIndex()), 0xFFFF40);
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = event.player;
            if (player == mc.player && player.world.isRemote) {
                boolean anyArmorEquipped = false;
                CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer();
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    if (isPneumaticArmorPiece(player, slot)) {
                        updateArmorPiece(mc.player, slot, comHudHandler);
                        anyArmorEquipped = true;
                    }
                }
                if (anyArmorEquipped) {
                    ensureArmorInit(player, comHudHandler);
                    updateLauncherTracker();
                    pendingMessages.forEach(message -> message.getStat().tickWidget());
                    pendingMessages.removeIf(message -> message == null || --message.lifeSpan <= 0);
                } else {
                    pendingMessages.clear();
                    sentForceInitPacket = false;
                }
            }
        }
    }

    private void ensureArmorInit(PlayerEntity player, CommonArmorHandler comHudHandler) {
        if (!isPneumaticArmorPiece(player, EquipmentSlotType.HEAD) && !sentForceInitPacket) {
            // Special case: ensure core components packet always gets sent so armor can switch on even if helmet
            // is not equipped (core components is in the helmet for historical reasons)
            boolean state = WidgetKeybindCheckBox.getCoreComponents().checked;
            // core-components is always in slot HEAD, index 0
            if (state) {
                comHudHandler.setUpgradeEnabled(EquipmentSlotType.HEAD, (byte) 0, true);
                NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) 0, true, EquipmentSlotType.HEAD));
            }
            sentForceInitPacket = true;
        }
    }

    private void updateLauncherTracker() {
        if (LauncherTracker.INSTANCE.getLauncherProgress() > 0) {
            if (!KeyHandler.getInstance().keybindLauncher.isKeyDown()) {
                LauncherTracker.INSTANCE.trigger();
            } else {
                LauncherTracker.INSTANCE.chargeLauncher();
            }
        }
    }

    private void updateArmorPiece(PlayerEntity player, EquipmentSlotType slot, CommonArmorHandler commonArmorHandler) {
        boolean armorEnabled = WidgetKeybindCheckBox.getCoreComponents().checked;
        List<IArmorUpgradeHandler> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);

        int ticksSinceEquipped = commonArmorHandler.getTicksSinceEquipped(slot);
        int startupTime = commonArmorHandler.getStartupTime(slot);
        ITextComponent itemName = player.getItemStackFromSlot(slot).getDisplayName();

        if (ticksSinceEquipped > startupTime && armorEnabled) {
            // After full init: tick the client handler for each installed upgrade and open/close stat windows as needed
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                IArmorUpgradeHandler upgradeHandler = upgradeHandlers.get(i);
                if (commonArmorHandler.isUpgradeInserted(slot, i) && commonArmorHandler.isUpgradeEnabled(slot, i)) {
                    IArmorUpgradeClientHandler clientHandler = ArmorUpgradeClientRegistry.getInstance().getClientHandler(upgradeHandler);
                    IGuiAnimatedStat stat = clientHandler.getAnimatedStat();
                    if (stat != null) {
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
        } else if (ticksSinceEquipped == startupTime) {
            // full init: display "init complete" message
            playArmorInitSound(player, ModSounds.HUD_INIT_COMPLETE.get(), 1.0F);
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initComplete", itemName), 50, 0x7000AA00));
        } else if (ticksSinceEquipped == 0) {
            // tick 0: inform the server which upgrades are enabled
            for (IArmorUpgradeClientHandler handler : ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot)) {
                handler.reset();
            }
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                boolean state = armorEnabled && WidgetKeybindCheckBox.forUpgrade(upgradeHandlers.get(i)).checked;
                commonArmorHandler.setUpgradeEnabled(slot, (byte) i, state);
                NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, state, slot));
            }
        } else if (ticksSinceEquipped == 1) {
            // tick 1: display the "init started" message
            playArmorInitSound(player, ModSounds.HUD_INIT.get(), 0.5F);
            addMessage(new ArmorMessage(xlate("pneumaticcraft.armor.message.initStarted", itemName), 50, 0x7000AA00));
        } else {
            // any other tick during startup: display found/not found message for each possible upgrade
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (ticksSinceEquipped == startupTime / (upgradeHandlers.size() + 2) * (i + 1)) {
                    playArmorInitSound(player, ModSounds.HUD_INIT.get(), 0.5F + (float) (i + 1) / (upgradeHandlers.size() + 2) * 0.5F);
                    boolean upgradeEnabled = commonArmorHandler.isUpgradeInserted(slot, i);
                    ITextComponent message = xlate(upgradeHandlers.get(i).getTranslationKey())
                            .appendString(upgradeEnabled ? " installed" : " not installed");
                    addMessage(new ArmorMessage(message, 80, upgradeEnabled ? 0x7000AA00 : 0x70FF8000));
                }
            }
        }
    }

    private void playArmorInitSound(PlayerEntity player, SoundEvent sound, float pitch) {
        // avoid playing sounds too often... if many upgrades are installed it could get really noisy
        long when = player.world.getGameTime();
        if (when - lastArmorInitSound >= 30) {
            player.world.playSound(player.getPosX(), player.getPosY(), player.getPosZ(), sound, SoundCategory.PLAYERS, 0.2F, pitch, true);
        }
        lastArmorInitSound = when;
    }

    public void addFeatureToggleMessage(String key, boolean enabled) {
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", xlate(key)),
                Collections.emptyList(), 60, 0x7000AA00);
    }

    public void addFeatureToggleMessage(String key, String subKey, boolean enabled) {
        ITextComponent msg = xlate(key).appendString(": ").append(xlate(subKey));
        addMessage(xlate("pneumaticcraft.armor.message." + (enabled ? "enable" : "disable") + "Setting", msg),
                Collections.emptyList(), 60, 0x7000AA00);
    }

    public void addMessage(ITextComponent title, List<ITextComponent> message, int duration, int backColor) {
        addMessage(new ArmorMessage(title, message, duration, backColor));
    }

    public void addMessage(ArmorMessage message) {
        if (pendingMessages.size() > 0) {
            message.setDependingMessage(pendingMessages.get(pendingMessages.size() - 1).getStat()); //set the depending stat of the new stat to the last stat.
        }
        pendingMessages.add(message);
    }

    @Override
    public void onKeyPress(KeyBinding key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isGameFocused()) {
            if (key == KeyHandler.getInstance().keybindOpenOptions) {
                if (ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(mc.player)) {
                    mc.displayGuiScreen(GuiArmorMainScreen.getInstance());
                }
            } else if (key == KeyHandler.getInstance().keybindHack && HackClientHandler.enabledForPlayer(mc.player)) {
                getSpecificRenderer(BlockTrackerClientHandler.class).hack();
                getSpecificRenderer(EntityTrackerClientHandler.class).hack();
            } else if (key == KeyHandler.getInstance().keybindDebuggingDrone
                    && DroneDebugClientHandler.enabledForPlayer(mc.player)) {
                getSpecificRenderer(EntityTrackerClientHandler.class).selectAsDebuggingTarget();
            } else if (key == KeyHandler.getInstance().keybindKick
                    && CommonArmorHandler.getHandlerForPlayer().getUpgradeCount(EquipmentSlotType.FEET, EnumUpgrade.DISPENSER) > 0) {
                NetworkHandler.sendToServer(new PacketPneumaticKick());
            } else if (key == KeyHandler.getInstance().keybindLauncher
                    && !mc.player.getHeldItemOffhand().isEmpty()
                    && LauncherTracker.INSTANCE.isPlayerOKToLaunch()) {
                LauncherTracker.INSTANCE.startCharging();
            }
        }
    }

    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollEvent event) {
        boolean isCaptured = getSpecificRenderer(BlockTrackerClientHandler.class).scroll(event);
        if (!isCaptured) isCaptured = getSpecificRenderer(EntityTrackerClientHandler.class).scroll(event);
        if (isCaptured) event.setCanceled(true);
    }

    @SubscribeEvent
    public void handleResolutionChange(GuiScreenEvent.InitGuiEvent event) {
        Screen gui = event.getGui();
        if (gui.getMinecraft().world != null) {
            MainWindow mw = gui.getMinecraft().getMainWindow();
            if (mw.getScaledWidth() != lastScaledWidth || mw.getScaledHeight() != lastScaledHeight) {
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    for (IArmorUpgradeClientHandler handler : ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot)) {
                        handler.onResolutionChanged();
                    }
                }
                lastScaledWidth = mw.getScaledWidth();
                lastScaledHeight = mw.getScaledHeight();
            }
        }
    }
}
