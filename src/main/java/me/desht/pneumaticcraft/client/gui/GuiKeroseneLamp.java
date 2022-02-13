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

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.KeroseneLampBlockEntity;
import me.desht.pneumaticcraft.common.inventory.KeroseneLampMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.gui.widget.Slider;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiKeroseneLamp extends GuiPneumaticContainerBase<KeroseneLampMenu,KeroseneLampBlockEntity> implements Slider.ISlider {

    private WidgetLabel rangeLabel;
    private Slider slider;

    public GuiKeroseneLamp(KeroseneLampMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addRenderableWidget(new WidgetTank(leftPos + 152, topPos + 15, te.getTank()));
        addRenderableWidget(rangeLabel = new WidgetLabel(leftPos + 20, topPos + 55, TextComponent.EMPTY));

        addRenderableWidget(slider = new Slider(leftPos + 7, topPos + 30, 118, 20,
                xlate("pneumaticcraft.gui.keroseneLamp.maxRange").append(" "), TextComponent.EMPTY,
                1, KeroseneLampBlockEntity.MAX_RANGE, te.getTargetRange(), false, true,
                b -> { }, this));
    }

    @Override
    public void containerTick() {
        if (firstUpdate) {
            // te sync packet hasn't necessarily arrived when init() is called; need to set it up here
            slider.setValue(te.getTargetRange());
            slider.updateSlider();
        }

        super.containerTick();

        rangeLabel.setMessage(xlate("pneumaticcraft.message.misc.range", te.getRange()));
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        sendDelayed(5);
    }

    @Override
    protected void doDelayedAction() {
        sendGUIButtonPacketToServer(Integer.toString(slider.getValueInt()));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_KEROSENE_LAMP;
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);
        if (te.getTank().getFluidAmount() == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.noFuel"));
        } else if (te.getFuelQuality() == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.badFuel"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);
        if (te.getTank().getFluidAmount() < 30 && te.getTank().getFluidAmount() > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.lowFuel"));
        }
    }
}
