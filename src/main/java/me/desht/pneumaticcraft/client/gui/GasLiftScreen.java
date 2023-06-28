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

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.GasLiftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.GasLiftBlockEntity.PumpMode;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.GasLiftMenu;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GasLiftScreen extends AbstractPneumaticCraftContainerScreen<GasLiftMenu,GasLiftBlockEntity> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[PumpMode.values().length];

    public GasLiftScreen(GasLiftMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new WidgetTank(leftPos + 80, topPos + 15, te.getTank()));
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.GAS_LIFT.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.gasLift.mode"), new ItemStack(ModBlocks.PRESSURE_TUBE.get()), 0xFFFFCC00, false);
        optionStat.setMinimumExpandedDimensions(60, 45);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, Component.empty()).withTag(PumpMode.PUMP_EMPTY.toString());
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.gasLift.mode.pumpEmpty")));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, Component.empty()).withTag(PumpMode.PUMP_LEAVE_FLUID.toString());
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.gasLift.mode.pumpLeave")));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;

        button = new WidgetButtonExtended(55, 20, 20, 20, Component.empty()).withTag(PumpMode.RETRACT.toString());
        button.setRenderStacks(new ItemStack(ModBlocks.DRILL_PIPE.get()));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.gasLift.mode.drawIn")));
        optionStat.addSubWidget(button);
        modeButtons[2] = button;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_GAS_LIFT;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -1);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        statusStat.setText(getStatus());
        for (int i = 0; i < modeButtons.length; i++) {
            modeButtons[i].active = te.pumpMode != PumpMode.values()[i];
        }
    }

    private List<Component> getStatus() {
        List<Component> textList = new ArrayList<>();
        textList.add(xlate("pneumaticcraft.gui.tab.status.gasLift.action"));
        textList.add(xlate(te.status.getTranslationKey(), te.getTank().getFluid().getDisplayName().getString()).withStyle(ChatFormatting.BLACK));
        textList.add(xlate("pneumaticcraft.gui.tab.status.gasLift.currentDepth"));
        textList.add(Component.literal(te.currentDepth + " meter(s)").withStyle(ChatFormatting.BLACK));
        return textList;
    }

    @Override
    public void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);
        if (te.pumpMode == PumpMode.PUMP_EMPTY || te.pumpMode == PumpMode.PUMP_LEAVE_FLUID) {
            if (te.getTank().getCapacity() - te.getTank().getFluidAmount() < 1000) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.gasLift.noLiquidSpace"));
            }
            if (te.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.gasLift.noTubes"));
            }
            if (te.status == GasLiftBlockEntity.Status.STUCK) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.gasLift.stuck"));
            }
        } else {
            if (te.getPrimaryInventory().getStackInSlot(0).getCount() == 64) {
                curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.gasLift.noTubeSpace"));
            }
        }
    }
}
