package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public enum LauncherTracker {
    INSTANCE;

    public static final int MAX_PROGRESS = 15; // ticks

    private int launcherProgress = 0;

    public int getLauncherProgress() {
        return launcherProgress;
    }

    public boolean isPlayerOKToLaunch() {
        if (launcherProgress > 0) return false;
        CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
        return handler.isArmorReady(EquipmentSlotType.CHEST)
                && handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.DISPENSER) > 0
                && handler.getArmorPressure(EquipmentSlotType.CHEST) > 0.1f;
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

    public void render(MatrixStack matrixStack, MainWindow sr, float partialTicks) {
        matrixStack.push();
        RenderSystem.disableTexture();
        if (Minecraft.getInstance().player.getPrimaryHand() == HandSide.LEFT) {
            matrixStack.translate(sr.getScaledWidth() - 30, sr.getScaledHeight() - 30, -90);
            matrixStack.scale(-1, 1, 1);
        } else {
            matrixStack.translate(30, sr.getScaledHeight() - 30, -90);
        }
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(-60));
        float progress = Math.min(100f, (launcherProgress + partialTicks) * 100f / LauncherTracker.MAX_PROGRESS);
        RenderProgressBar.render2d(matrixStack, 0, 0, sr.getScaledWidth() / 6f - 30, 12, 0,
                progress, 0xAA0000A0, 0xAA40A0FF);
        RenderSystem.enableTexture();
        matrixStack.pop();
    }
}
