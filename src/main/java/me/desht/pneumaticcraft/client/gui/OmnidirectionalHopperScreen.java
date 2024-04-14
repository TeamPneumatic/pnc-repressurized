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

import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.hopper.OmnidirectionalHopperBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.inventory.OmnidirectionalHopperMenu;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
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

public class OmnidirectionalHopperScreen extends AbstractPneumaticCraftContainerScreen<OmnidirectionalHopperMenu,OmnidirectionalHopperBlockEntity> {
    private static final Component ARROW_NO_RR = Component.literal(Symbols.ARROW_RIGHT);
    private static final Component ARROW_RR = Component.literal(Symbols.CIRCULAR_ARROW).withStyle(ChatFormatting.GREEN);

    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];
    private WidgetButtonExtended rrButton;

    public OmnidirectionalHopperScreen(OmnidirectionalHopperMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.hopperStatus"), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.gasLift.mode"), new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.setMinimumExpandedDimensions(50, 43);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, Component.empty()).withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.empty")));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, Component.empty()).withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.leaveItem")));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;

        addRenderableWidget(rrButton = new WidgetButtonExtended(leftPos + 143, topPos + 55, 14, 14, Component.empty()).withTag("rr"));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_OMNIDIRECTIONAL_HOPPER;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        statusStat.setText(getStatus());
        modeButtons[0].active = te.doesLeaveMaterial();
        modeButtons[1].active = !te.doesLeaveMaterial();
        rrButton.setMessage(te.roundRobin ? ARROW_RR : ARROW_NO_RR);
        rrButton.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tooltip.omnidirectional_hopper.roundRobin." + (te.roundRobin ? "on" : "off"))));
    }

    private List<Component> getStatus() {
        List<Component> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerTick", itemsPer));
        } else {
            int transferInterval = te.getItemTransferInterval();
            String s = transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1);
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerSecond", s));
        }
        return textList;
    }

    @Override
    protected boolean isUpgradeAvailable(PNCUpgrade upgrade) {
        return upgrade != ModUpgrades.DISPENSER.get() || ConfigHelper.common().machines.omniHopperDispenser.get();
    }
}
