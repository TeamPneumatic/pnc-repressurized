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
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.HackOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.HackHandler;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<HackHandler> {
    public HackClientHandler() {
        super(CommonUpgradeHandlers.hackHandler);
    }

    @Override
    public Optional<KeyMapping> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindHack);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (enabledForPlayer(armorHandler.getPlayer())) {
            ClientArmorRegistry c = ClientArmorRegistry.getInstance();
            c.getClientHandler(CommonUpgradeHandlers.blockTrackerHandler, BlockTrackerClientHandler.class).hack();
            c.getClientHandler(CommonUpgradeHandlers.entityTrackerHandler, EntityTrackerClientHandler.class).hack();
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
        return new HackOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    public static boolean enabledForPlayer(Player player) {
        return PneumaticArmorItem.isPneumaticArmorPiece(player, EquipmentSlot.HEAD)
                && CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(EquipmentSlot.HEAD, ModUpgrades.SECURITY.get()) > 0;
    }

    public static void addKeybindTooltip(List<Component> curInfo) {
        KeyMapping hack = KeyHandler.getInstance().keybindHack;
        if (hack.getKey().getValue() != 0) {
            MutableComponent str = xlate("pneumaticcraft.armor.hacking.pressToHack", ClientUtils.translateKeyBind(hack));
            curInfo.add(str.withStyle(ChatFormatting.GOLD));
        }
    }
}
