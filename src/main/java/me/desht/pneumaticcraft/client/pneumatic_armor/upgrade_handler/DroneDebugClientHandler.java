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

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.ArmorMainScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.DroneDebuggerOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.DroneDebugHandler;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DroneDebugClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<DroneDebugHandler> {
    private final Set<BlockPos> shownPositions = new HashSet<>();
    private final Set<BlockPos> shownArea = new HashSet<>();

    public DroneDebugClientHandler() {
        super(CommonUpgradeHandlers.droneDebugHandler);
    }

    public static void onWidgetsChanged() {
        if (Minecraft.getInstance().screen instanceof ArmorMainScreen a && a.getCurrentOptionsPage().page() instanceof DroneDebuggerOptions db) {
            IDroneBase drone = db.getSelectedDrone();
            if (drone != null) {
                ProgWidgetUtils.updatePuzzleConnections(drone.getProgWidgets());
                db.gotoStartWidget();
            }
        }
    }

    public Set<BlockPos> getShowingPositions() {
        return shownPositions;
    }

    public Set<BlockPos> getShownArea() {
        return shownArea;
    }

    @Override
    public Optional<KeyMapping> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindDebuggingDrone);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (enabledForPlayer(Minecraft.getInstance().player)) {
            ClientArmorRegistry c = ClientArmorRegistry.getInstance();
            ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();
            c.getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class).selectAsDebuggingTarget();
        }
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
    }

    @Override
    public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
    }

    @Override
    public void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure) {
    }

    @Override
    public void reset() {
    }

    @Override
    public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
        return new DroneDebuggerOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    public static boolean enabledForPlayer(Player player) {
        if (PneumaticArmorItem.isPneumaticArmorPiece(player, EquipmentSlot.HEAD)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            return handler.getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.DISPENSER.get()) > 0;
        } else {
            return false;
        }
    }
}
