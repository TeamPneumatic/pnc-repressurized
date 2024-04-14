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

import me.desht.pneumaticcraft.client.gui.widget.PNCForgeSlider;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.UVLightBoxBlock;
import me.desht.pneumaticcraft.common.block.entity.processing.UVLightBoxBlockEntity;
import me.desht.pneumaticcraft.common.inventory.UVLightBoxMenu;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class UVLightBoxScreen extends AbstractPneumaticCraftContainerScreen<UVLightBoxMenu,UVLightBoxBlockEntity> {
    private PNCForgeSlider slider;

    public UVLightBoxScreen(UVLightBoxMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 196;
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(slider = new PNCForgeSlider(leftPos + 10, topPos + 45, 95, 16,
                xlate("pneumaticcraft.gui.uv_light_box.threshold").append(" "), Component.literal("%"),
                1, 100, te.getThreshold(), true, slider -> sendDelayed(5)));
    }

    @Override
    public void containerTick() {
        boolean interpolate = te.rsController.getCurrentMode() == UVLightBoxBlockEntity.RS_MODE_INTERPOLATE;
        if (firstUpdate || interpolate) {
            // te sync packet hasn't necessarily arrived when init() is called; need to set it up here
            slider.setValue(te.getThreshold());
        }
        slider.active = !interpolate;
        slider.visible = !interpolate || te.getRedstoneController().getCurrentRedstonePower() > 0;

        super.containerTick();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        BlockState state = te.getBlockState();
        return state.getBlock() == ModBlocks.UV_LIGHT_BOX.get() && state.getValue(UVLightBoxBlock.LIT) ?
                Textures.GUI_UV_LIGHT_BOX_ON : Textures.GUI_UV_LIGHT_BOX;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 10, yStart + imageHeight / 4 - 5);
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);

        if (te.getItemHandler().getStackInSlot(UVLightBoxBlockEntity.PCB_SLOT).isEmpty()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.uv_light_box.no_item"));
        }
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        BlockState state = te.getBlockState();
        if (state.getBlock() instanceof UVLightBoxBlock && state.getValue(UVLightBoxBlock.LIT)) {
            float usage = PneumaticValues.USAGE_UV_LIGHTBOX * te.getSpeedUsageMultiplierFromUpgrades();
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticCraftUtils.roundNumberTo(usage, 2)));
        }
    }

    @Override
    protected void doDelayedAction() {
        sendGUIButtonPacketToServer(Integer.toString(slider.getValueInt()));
    }
}
