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
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.HackOptions;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.HackHandler;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class HackClientHandler extends IArmorUpgradeClientHandler.AbstractHandler<HackHandler> {
    public HackClientHandler() {
        super(ArmorUpgradeRegistry.getInstance().hackHandler);
    }

    @Override
    public Optional<KeyBinding> getTriggerKeyBinding() {
        return Optional.of(KeyHandler.getInstance().keybindHack);
    }

    @Override
    public void onTriggered(ICommonArmorHandler armorHandler) {
        if (enabledForPlayer(armorHandler.getPlayer())) {
            ArmorUpgradeClientRegistry c = ArmorUpgradeClientRegistry.getInstance();
            ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();
            c.getClientHandler(r.blockTrackerHandler, BlockTrackerClientHandler.class).hack();
            c.getClientHandler(r.entityTrackerHandler, EntityTrackerClientHandler.class).hack();
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
        return new HackOptions(screen, this);
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    public static boolean enabledForPlayer(PlayerEntity player) {
        return ItemPneumaticArmor.isPneumaticArmorPiece(player, EquipmentSlotType.HEAD)
                && CommonArmorHandler.getHandlerForPlayer(player).getUpgradeCount(EquipmentSlotType.HEAD, EnumUpgrade.SECURITY) > 0;
    }

    public static void addKeybindTooltip(List<ITextComponent> curInfo) {
        KeyBinding hack = KeyHandler.getInstance().keybindHack;
        if (hack.getKey().getValue() != 0) {
            IFormattableTextComponent str = xlate("pneumaticcraft.armor.hacking.pressToHack", ClientUtils.translateKeyBind(hack));
            curInfo.add(str.withStyle(TextFormatting.GOLD));
        }
    }
}
