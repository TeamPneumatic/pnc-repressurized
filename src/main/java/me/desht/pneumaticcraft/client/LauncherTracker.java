package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumHandSide;

public enum LauncherTracker {
    INSTANCE;

    public static final int MAX_PROGRESS = 15; // ticks

    private int launcherProgress = 0;

    public int getLauncherProgress() {
        return launcherProgress;
    }

    public boolean isPlayerOKToLaunch() {
        if (launcherProgress > 0) return false;
        CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer();
        return handler.isArmorReady(EntityEquipmentSlot.CHEST)
                && handler.getUpgradeCount(EntityEquipmentSlot.CHEST, IItemRegistry.EnumUpgrade.DISPENSER) > 0
                && handler.getArmorPressure(EntityEquipmentSlot.CHEST) > 0.1f;
    }

    public void startCharging() {
        if (KeyHandler.getInstance().keybindLauncher.isKeyDown()) {
            launcherProgress++;
        }
    }

    public void chargeLauncher() {
        if (launcherProgress > 0 && launcherProgress < MAX_PROGRESS) {
            launcherProgress++;
        }
    }

    public void trigger() {
        NetworkHandler.sendToServer(new PacketChestplateLauncher((float) launcherProgress / (float) MAX_PROGRESS));
        launcherProgress = 0;
    }

    public void render(ScaledResolution sr, float partialTicks) {
        GlStateManager.pushMatrix();
        if (Minecraft.getMinecraft().player.getPrimaryHand() == EnumHandSide.LEFT) {
            GlStateManager.translate(sr.getScaledWidth() - 30, sr.getScaledHeight() - 30, -90);
            GlStateManager.scale(-1, 1, 1);
        } else {
            GlStateManager.translate(30, sr.getScaledHeight() - 30, -90);
        }
        GlStateManager.rotate(-60, 0, 0, 1);
        float progress = Math.min(100f, (launcherProgress + partialTicks) * 100f / LauncherTracker.MAX_PROGRESS);
        RenderProgressBar.render(0, 0, sr.getScaledWidth_double() / 6 - 30, 12, 0,
                progress, 0xAA0000A0, 0xAA40A0FF);
        GlStateManager.popMatrix();
    }
}
