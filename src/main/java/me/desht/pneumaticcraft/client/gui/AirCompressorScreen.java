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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.AirCompressorBlockEntity;
import me.desht.pneumaticcraft.common.inventory.AirCompressorMenu;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirCompressorScreen extends AbstractPneumaticCraftContainerScreen<AirCompressorMenu,AirCompressorBlockEntity> {

    public AirCompressorScreen(AirCompressorMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_COMPRESSOR;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);

        int yOff = te.getBurnTimeRemainingScaled(12);
        if (te.burnTime >= te.curFuelUsage) {
            graphics.blit(getGuiTexture(), leftPos + getFuelSlotXOffset(), topPos + 38 + 12 - yOff, 176, 12 - yOff, 14, yOff + 2);
        }
    }

    protected int getFuelSlotXOffset() {
        return 80;
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.maxProduction",
                PneumaticCraftUtils.roundNumberTo(te.airPerTick, 2)).withStyle(ChatFormatting.BLACK));
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);
        if (te.burnTime <= te.curFuelUsage && !FurnaceBlockEntity.isFuel(te.getPrimaryInventory().getStackInSlot(0))) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.airCompressor.noFuel"));
        }

        if (te.hasNoConnectedAirHandlers()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.airLeak"));
        }
    }

    @Override
    protected String upgradeCategory() {
        return "air_compressor";
    }
}
