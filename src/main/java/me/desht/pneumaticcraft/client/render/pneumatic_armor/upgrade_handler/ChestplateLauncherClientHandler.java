package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.ChestplateLauncherOptions;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ChestplateLauncherHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class ChestplateLauncherClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<ChestplateLauncherHandler> {
    public static final int MAX_PROGRESS = 15; // ticks

    private int launcherProgress = 0;

    public ChestplateLauncherClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().chestplateLauncherHandler);
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new ChestplateLauncherOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        if (launcherProgress > 0) {
            if (!KeyHandler.getInstance().keybindLauncher.isKeyDown()) {
                NetworkHandler.sendToServer(new PacketChestplateLauncher((float) launcherProgress / (float) MAX_PROGRESS));
                launcherProgress = 0;
            } else if (launcherProgress > 0 && launcherProgress < MAX_PROGRESS) {
                launcherProgress++;
            }
        }
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
        if (launcherProgress == 0) return;

        MainWindow mw = Minecraft.getInstance().getMainWindow();

        matrixStack.push();
        RenderSystem.disableTexture();
        if (Minecraft.getInstance().player.getPrimaryHand() == HandSide.LEFT) {
            matrixStack.translate(mw.getScaledWidth() - 30, mw.getScaledHeight() - 30, -90);
            matrixStack.scale(-1, 1, 1);
        } else {
            matrixStack.translate(30, mw.getScaledHeight() - 30, -90);
        }
        matrixStack.rotate(Vector3f.ZP.rotationDegrees(-60));
        float progress = Math.min(100f, (launcherProgress + partialTicks) * 100f / MAX_PROGRESS);
        RenderProgressBar.render2d(matrixStack, 0, 0, mw.getScaledWidth() / 6f - 30, 12, 0,
                progress, 0xAA0000A0, 0xAA40A0FF);
        RenderSystem.enableTexture();
        matrixStack.pop();
    }

    public void maybeStartCharging(KeyBinding key) {
        if (launcherProgress == 0) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer();
            if (handler.isArmorReady(EquipmentSlotType.CHEST)
                    && handler.getUpgradeCount(EquipmentSlotType.CHEST, EnumUpgrade.DISPENSER) > 0
                    && handler.getArmorPressure(EquipmentSlotType.CHEST) > 0.1f
                    && key.isKeyDown()) {
                launcherProgress = 1;
            }
        }
    }
}
