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

package me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.DroneDebuggerOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.DroneDebugHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DroneDebugClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<DroneDebugHandler> {
    private final Set<BlockPos> shownPositions = new HashSet<>();
    private final Set<BlockPos> shownArea = new HashSet<>();

    public DroneDebugClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().droneDebugHandler);
    }

    public Set<BlockPos> getShowingPositions() {
        return shownPositions;
    }

    public Set<BlockPos> getShownArea() {
        return shownArea;
    }

    @Override
    public Optional<KeyBinding> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindDebuggingDrone);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (enabledForPlayer(Minecraft.getInstance().player)) {
            ArmorUpgradeClientRegistry c = ArmorUpgradeClientRegistry.getInstance();
            ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();
            c.getClientHandler(r.entityTrackerHandler, EntityTrackerClientHandler.class).selectAsDebuggingTarget();
        }
    }

    @Override
    public void tickClient(ICommonArmorHandler armorHandler) {
    }

    @Override
    public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
    }

    @Override
    public void render2D(MatrixStack matrixStack, float partialTicks, boolean armorPieceHasPressure) {
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

    public static boolean enabledForPlayer(PlayerEntity player) {
        if (ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            return handler.getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.DISPENSER) > 0;
        } else {
            return false;
        }
    }
}
