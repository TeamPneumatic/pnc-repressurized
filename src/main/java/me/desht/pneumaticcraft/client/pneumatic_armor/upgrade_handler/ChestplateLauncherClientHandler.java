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

package me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.ChestplateLauncherOptions;
import me.desht.pneumaticcraft.client.render.ProgressBarRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketChestplateLauncher;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.ChestplateLauncherHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.HumanoidArm;

import java.util.Optional;

public class ChestplateLauncherClientHandler extends IArmorUpgradeClientHandler.SimpleToggleableHandler<ChestplateLauncherHandler> {
    public static final int MAX_PROGRESS = 15; // ticks

    private int launcherProgress = 0;

    public ChestplateLauncherClientHandler() {
        super(CommonUpgradeHandlers.chestplateLauncherHandler);
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
    public Optional<KeyMapping> getTriggerKeyBinding() {
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
    public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
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
    public void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure) {
        if (launcherProgress == 0) return;

        Window mw = Minecraft.getInstance().getWindow();

        graphics.pose().pushPose();
        if (ClientUtils.getClientPlayer().getMainArm() == HumanoidArm.LEFT) {
            graphics.pose().translate(mw.getGuiScaledWidth() - 30, mw.getGuiScaledHeight() - 30, -90);
            graphics.pose().scale(-1, 1, 1);
        } else {
            graphics.pose().translate(30, mw.getGuiScaledHeight() - 30, -90);
        }
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(-60));
        float progress = Math.min(100f, (launcherProgress + partialTicks) * 100f / MAX_PROGRESS);
        ProgressBarRenderer.render2d(graphics, 0, 0, mw.getGuiScaledWidth() / 6f - 30, 12, 0,
                progress, 0xAA0000A0, 0xAA40A0FF);
        graphics.pose().popPose();
    }
}
