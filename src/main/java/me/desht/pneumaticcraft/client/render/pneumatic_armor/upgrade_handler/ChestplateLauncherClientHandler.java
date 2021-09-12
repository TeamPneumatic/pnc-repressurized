package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.ChestplateLauncherOptions;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ChestplateLauncherHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

import java.util.Optional;

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
    public Optional<KeyBinding> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindLauncher);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (!armorHandler.getPlayer().getOffhandItem().isEmpty()
                && launcherProgress == 0
                && armorHandler.upgradeUsable(getCommonHandler(), false)) {
            launcherProgress = 1;
        }
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
        if (launcherProgress > 0) {
            if (!KeyHandler.getInstance().keybindLauncher.isDown()) {
                NetworkHandler.sendToServer(new PacketChestplateLauncher((float) launcherProgress / (float) MAX_PROGRESS));
                launcherProgress = 0;
            } else {
                launcherProgress = Math.min(launcherProgress + 1, MAX_PROGRESS);
            }
        }
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
        if (launcherProgress == 0) return;

        MainWindow mw = Minecraft.getInstance().getWindow();

        matrixStack.pushPose();
        RenderSystem.disableTexture();
        if (Minecraft.getInstance().player.getMainArm() == HandSide.LEFT) {
            matrixStack.translate(mw.getGuiScaledWidth() - 30, mw.getGuiScaledHeight() - 30, -90);
            matrixStack.scale(-1, 1, 1);
        } else {
            matrixStack.translate(30, mw.getGuiScaledHeight() - 30, -90);
        }
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-60));
        float progress = Math.min(100f, (launcherProgress + partialTicks) * 100f / MAX_PROGRESS);
        RenderProgressBar.render2d(matrixStack, 0, 0, mw.getGuiScaledWidth() / 6f - 30, 12, 0,
                progress, 0xAA0000A0, 0xAA40A0FF);
        RenderSystem.enableTexture();
        matrixStack.popPose();
    }
}
