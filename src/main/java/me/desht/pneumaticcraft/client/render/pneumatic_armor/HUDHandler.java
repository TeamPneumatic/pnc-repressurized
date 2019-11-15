package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.IKeyListener;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.LauncherTracker;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.DroneDebugUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.HackUpgradeHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPneumaticKick;
import me.desht.pneumaticcraft.common.network.PacketToggleArmorFeature;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.item.ItemPneumaticArmor.isPneumaticArmorPiece;
import static net.minecraft.client.Minecraft.IS_RUNNING_ON_MAC;

public class HUDHandler implements IKeyListener {
    private static final int PROGRESS_BAR_HEIGHT = 17;

    private long lastArmorInitSound; // avoid too much sound spam when equipping armor
    private boolean sentForceInitPacket = false; // whether to send an armor init packet when helmet not equipped

    private final List<ArmorMessage> messageList = new ArrayList<>();
    private final boolean[] gaveEmptyWarning = new boolean[4];  // per-slot
    private final boolean[] gaveNearlyEmptyWarning = new boolean[4];  // per-slot

    private static final HUDHandler INSTANCE = new HUDHandler();

    public static HUDHandler instance() {
        return INSTANCE;
    }

    public <T extends IUpgradeRenderHandler> T getSpecificRenderer(Class<T> clazz) {
        return UpgradeRenderHandlerList.instance().getRenderHandler(clazz);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (!GuiKeybindCheckBox.getCoreComponents().checked || mc.gameSettings.hideGUI) return;

        PlayerEntity player = mc.player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.translated(-playerX, -playerY, -playerZ);

        ItemStack helmetStack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
        CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer(player);
        if (helmetStack.getItem() instanceof ItemPneumaticArmor && comHudHandler.getArmorPressure(EquipmentSlotType.HEAD) > 0F) {
            for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                if (comHudHandler.isArmorReady(slot)) {
                    GlStateManager.disableTexture();
                    List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        if (comHudHandler.isUpgradeRendererInserted(slot, i) && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked)
                            renderHandlers.get(i).render3D(event.getPartialTicks());
                    }
                    GlStateManager.enableTexture();
                }
            }
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.END && !mc.gameSettings.hideGUI && mc.player != null) {
            render2D(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            PlayerEntity player = event.player;
            if (player == mc.player && player.world.isRemote) {
                boolean armorEquipped = false;
                CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer();
                for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    if (isPneumaticArmorPiece(player, slot)) {
                        update(mc.player, slot, comHudHandler);
                        armorEquipped = true;
                    }
                }
                if (armorEquipped) {
                    ensureArmorInit(player, comHudHandler);
                    updateLauncherTracker();
                    messageList.forEach(message -> message.getStat().tick());
                    messageList.removeIf(message -> message == null || --message.lifeSpan <= 0);
                } else {
                    messageList.clear();
                    sentForceInitPacket = false;
                }
            }
        }
    }

    private void ensureArmorInit(PlayerEntity player, CommonArmorHandler comHudHandler) {
        if (!isPneumaticArmorPiece(player, EquipmentSlotType.HEAD) && !sentForceInitPacket) {
            // Special case: ensure core components packet always gets sent so armor can switch on even if helmet
            // is not equipped (core components is in the helmet for historical reasons)
            boolean state = GuiKeybindCheckBox.getCoreComponents().checked;
            // core-components is always in slot HEAD, index 0
            if (state) {
                comHudHandler.setUpgradeRenderEnabled(EquipmentSlotType.HEAD, (byte) 0, true);
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

    private void render2D(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        if (mc.isGameFocused() && ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(player)) {
            MainWindow mw = mc.mainWindow;
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            GlStateManager.disableTexture();
            GlStateManager.pushMatrix();
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT, IS_RUNNING_ON_MAC);
            GlStateManager.color4f(0, 1, 0, 0.8F);
            CommonArmorHandler comHudHandler = CommonArmorHandler.getHandlerForPlayer(player);

            boolean anyArmorInInit = false;
            for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                ItemStack armorStack = player.getItemStackFromSlot(slot);
                if (armorStack.getItem() instanceof ItemPneumaticArmor && !comHudHandler.isArmorReady(slot)) {
                    anyArmorInInit = true;
                    break;
                }
            }
            for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                ItemStack armorStack = player.getItemStackFromSlot(slot);
                if (!(armorStack.getItem() instanceof ItemPneumaticArmor) || comHudHandler.getArmorPressure(slot) < 0.0001F) {
                    continue;
                }
                if (anyArmorInInit) {
                    // initialization progress bar(s)
                    gaveEmptyWarning[slot.getIndex()] = false;
                    gaveNearlyEmptyWarning[slot.getIndex()] = false;
                    if (comHudHandler.isArmorEnabled()) {
                        int xLeft = mw.getScaledWidth() / 2;
                        int yOffset = 10 + (3 - slot.getIndex()) * PROGRESS_BAR_HEIGHT;
                        float progress = comHudHandler.getTicksSinceEquipped(slot) * 100f / comHudHandler.getStartupTime(slot);
                        progress = Math.min(100, progress + partialTicks);
                        RenderProgressBar.render(mw.getScaledWidth() / 2.0, yOffset,
                                mw.getScaledWidth() - 10, yOffset + PROGRESS_BAR_HEIGHT - 1, -90F,
                                progress,0xAAFFC000, 0xAA00FF00);
                        GlStateManager.enableTexture();
                        GuiUtils.drawItemStack(armorStack,xLeft + 2, yOffset);
                    }
                }
                if (comHudHandler.isArmorReady(slot)) {
                    String itemName = armorStack.getDisplayName().getFormattedText();
                    float pressure = comHudHandler.armorPressure[slot.getIndex()];
                    // low/no pressure warnings
                    if (pressure < 0.05F && !gaveEmptyWarning[slot.getIndex()]) {
                        addMessage(new ArmorMessage("Your " + itemName + " is out of air!", new ArrayList<>(), 100, 0x70FF0000));
                        gaveEmptyWarning[slot.getIndex()] = true;
                    }
                    if (pressure > 0.2F && pressure < 0.5F && !gaveNearlyEmptyWarning[slot.getIndex()]) {
                        addMessage(new ArmorMessage("Your " + itemName + " is almost out of air!", new ArrayList<>(), 60, 0x70FF8000));
                        gaveNearlyEmptyWarning[slot.getIndex()] = true;
                    }
                    // all enabled upgrades do their 2D rendering here
                    if (GuiKeybindCheckBox.getCoreComponents().checked) {
                        List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                        for (int i = 0; i < renderHandlers.size(); i++) {
                            IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                            if (comHudHandler.isUpgradeRendererInserted(slot, i) && comHudHandler.isUpgradeRendererEnabled(slot, i)) {
                                IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                                if (stat != null) {
                                    stat.render(-1, -1, partialTicks);
                                }
                                upgradeRenderHandler.render2D(partialTicks, pressure > 0F);
                            }
                        }
                    }
                }
            }

            // chestplate launcher upgrade (if installed)
            if (LauncherTracker.INSTANCE.getLauncherProgress() > 0) {
                LauncherTracker.INSTANCE.render(mw, partialTicks);
            }

            // render every pending message
            for (ArmorMessage message : messageList) {
                message.renderMessage(partialTicks);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture();

            // show armor initialisation percentages
            if (comHudHandler.isArmorEnabled() && anyArmorInInit) {
                for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    if (isPneumaticArmorPiece(player, slot) && comHudHandler.getArmorPressure(slot) > 0F) {
                        String text = Math.min(100, comHudHandler.getTicksSinceEquipped(slot) * 100 / comHudHandler.getStartupTime(slot)) + "%";
                        mc.fontRenderer.drawStringWithShadow(text, mw.getScaledWidth() * 0.75f - 8, 14 + PROGRESS_BAR_HEIGHT * (3 - slot.getIndex()), 0xFFFF40);
                    }
                }
            }
        }
    }

    private void update(PlayerEntity player, EquipmentSlotType slot, CommonArmorHandler comHudHandler) {
        boolean armorEnabled = GuiKeybindCheckBox.getCoreComponents().checked;
        List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);

        // At start of init, inform the server which upgrades are enabled
        if (comHudHandler.getTicksSinceEquipped(slot) == 0) {
            for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().getHandlersForSlot(slot)) {
                handler.reset();
            }
            for (int i = 0; i < renderHandlers.size(); i++) {
                boolean state = armorEnabled && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked;
                comHudHandler.setUpgradeRenderEnabled(slot, (byte) i, state);
                NetworkHandler.sendToServer(new PacketToggleArmorFeature((byte) i, state, slot));
            }
        }

        // After full init, run handler's update() on each installed upgrade
        if (comHudHandler.getTicksSinceEquipped(slot) > comHudHandler.getStartupTime(slot) && armorEnabled) {
            for (int i = 0; i < renderHandlers.size(); i++) {
                IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                if (comHudHandler.isUpgradeRendererInserted(slot, i) && comHudHandler.isUpgradeRendererEnabled(slot, i)) {
                    IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                    if (stat != null) {
                        if (comHudHandler.armorPressure[slot.getIndex()] > upgradeRenderHandler.getMinimumPressure()) {
                            stat.openWindow();
                        } else {
                            stat.closeWindow();
                        }
                        stat.tick();
                    }
                    upgradeRenderHandler.update(player, comHudHandler.getUpgradeCount(slot, IItemRegistry.EnumUpgrade.RANGE));
                }
            }
        }

        // During init, display found/not found message for each possible upgrade
        for (int i = 0; i < renderHandlers.size(); i++) {
            if (comHudHandler.getTicksSinceEquipped(slot) == comHudHandler.getStartupTime(slot) / (renderHandlers.size() + 2) * (i + 1)) {
                playArmorInitSound(player, ModSounds.HUD_INIT, 0.5F + (float) (i + 1) / (renderHandlers.size() + 2) * 0.5F);
                boolean upgradeEnabled = comHudHandler.isUpgradeRendererInserted(slot, i);
                addMessage(new ArmorMessage(I18n.format(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()) + (upgradeEnabled ? " installed" : " not installed"), new ArrayList<>(), 80, upgradeEnabled ? 0x7000AA00 : 0x70FF8000));
            }
        }

        ItemStack stack = player.getItemStackFromSlot(slot);

        if (comHudHandler.getTicksSinceEquipped(slot) == 1) {
            playArmorInitSound(player, ModSounds.HUD_INIT, 0.5F);
            addMessage(new ArmorMessage("Initializing " + stack.getDisplayName() + "...", Collections.emptyList(), 50, 0x7000AA00));
        }

        if (comHudHandler.getTicksSinceEquipped(slot) == comHudHandler.getStartupTime(slot)) {
            playArmorInitSound(player, ModSounds.HUD_INIT_COMPLETE, 1.0F);
            addMessage(new ArmorMessage(stack.getDisplayName() + " initialization complete!", Collections.emptyList(), 50, 0x7000AA00));
        }
    }

    private void playArmorInitSound(PlayerEntity player, SoundEvent sound, float pitch) {
        long when = player.world.getGameTime();
        if (when - lastArmorInitSound >= 30) {
            player.world.playSound(player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, 0.2F, pitch, true);
        }
        lastArmorInitSound = when;
    }

    public void addFeatureToggleMessage(String key, boolean enabled) {
        HUDHandler.instance().addMessage(I18n.format("pneumaticHelmet.message." + (enabled ? "enable" : "disable") + "Setting", I18n.format(key)), new ArrayList<>(), 60, 0x7000AA00);
    }

    public void addFeatureToggleMessage(IUpgradeRenderHandler handler, String key, boolean enabled) {
        String msg = I18n.format(GuiKeybindCheckBox.UPGRADE_PREFIX + handler.getUpgradeName()) + ": " + I18n.format(key);
        HUDHandler.instance().addMessage(I18n.format("pneumaticHelmet.message." + (enabled ? "enable" : "disable") + "Setting", msg), new ArrayList<>(), 60, 0x7000AA00);
    }

    public void addMessage(String title, List<String> message, int duration, int backColor) {
        addMessage(new ArmorMessage(title, message, duration, backColor));
    }

    public void addMessage(ArmorMessage message) {
        if (messageList.size() > 0) {
            message.setDependingMessage(messageList.get(messageList.size() - 1).getStat()); //set the depending stat of the new stat to the last stat.
        }
        messageList.add(message);
    }

    @Override
    public void onKeyPress(KeyBinding key) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isGameFocused()) {
            if (key == KeyHandler.getInstance().keybindOpenOptions) {
                if (ItemPneumaticArmor.isPlayerWearingAnyPneumaticArmor(mc.player)) {
                    mc.displayGuiScreen(GuiHelmetMainScreen.getInstance());
                }
            } else if (key == KeyHandler.getInstance().keybindHack && HackUpgradeHandler.enabledForPlayer(mc.player)) {
                getSpecificRenderer(BlockTrackUpgradeHandler.class).hack();
                getSpecificRenderer(EntityTrackUpgradeHandler.class).hack();
            } else if (key == KeyHandler.getInstance().keybindDebuggingDrone
                    && DroneDebugUpgradeHandler.enabledForPlayer(PneumaticCraftRepressurized.proxy.getClientPlayer())) {
                getSpecificRenderer(EntityTrackUpgradeHandler.class).selectAsDebuggingTarget();
            } else if (key == KeyHandler.getInstance().keybindKick
                    && CommonArmorHandler.getHandlerForPlayer().getUpgradeCount(EquipmentSlotType.FEET, IItemRegistry.EnumUpgrade.DISPENSER) > 0) {
                NetworkHandler.sendToServer(new PacketPneumaticKick());
            } else if (key == KeyHandler.getInstance().keybindLauncher
                    && !mc.player.getHeldItemOffhand().isEmpty()
                    && LauncherTracker.INSTANCE.isPlayerOKToLaunch()) {
                LauncherTracker.INSTANCE.startCharging();
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(GuiScreenEvent.MouseScrollEvent.Post event) {
        boolean isCaptured = getSpecificRenderer(BlockTrackUpgradeHandler.class).scroll(event);
        if (!isCaptured) isCaptured = getSpecificRenderer(EntityTrackUpgradeHandler.class).scroll(event);
        if (isCaptured) event.setCanceled(true);
    }

    public void onResolutionChanged() {
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().getHandlersForSlot(slot)) {
                handler.onResolutionChanged();
            }
        }
    }
}
