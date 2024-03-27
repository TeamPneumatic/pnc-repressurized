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

import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.LiquidHopperBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.LiquidHopperMenu;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class LiquidHopperScreen extends AbstractPneumaticCraftContainerScreen<LiquidHopperMenu,LiquidHopperBlockEntity> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];

    public LiquidHopperScreen(LiquidHopperMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addRenderableWidget(new WidgetTank(leftPos + 116, topPos + 15, te.getTank()));
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.hopperStatus"), new ItemStack(ModBlocks.LIQUID_HOPPER.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.gasLift.mode"), new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.setMinimumExpandedDimensions(50, 43);

        WidgetButtonExtended button = new WidgetButtonExtended(20, 20, 20, 20, Component.empty()).withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.liquidHopper.mode.empty")));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(45, 20, 20, 20, Component.empty()).withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.liquidHopper.mode.leaveLiquid")));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;
    }

    @Override
    protected boolean isUpgradeAvailable(PNCUpgrade upgrade) {
        return upgrade != ModUpgrades.DISPENSER.get() || ConfigHelper.common().machines.liquidHopperDispenser.get();
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -1);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        statusStat.setText(getStatus());
        modeButtons[0].active = te.doesLeaveMaterial();
        modeButtons[1].active = !te.doesLeaveMaterial();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_HOPPER;
    }

    private List<Component> getStatus() {
        List<Component> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.liquidTransferPerTick", itemsPer * 100));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.liquidTransferPerSecond", transferInterval == 0 ? "2000" : PneumaticCraftUtils.roundNumberTo(2000F / transferInterval, 1)));
        }
        return textList;
    }
}
