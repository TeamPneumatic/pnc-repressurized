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
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.AirCannonBlockEntity;
import me.desht.pneumaticcraft.common.inventory.AirCannonMenu;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AirCannonScreen extends AbstractPneumaticCraftContainerScreen<AirCannonMenu,AirCannonBlockEntity> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat strengthTab;
    private int gpsX;
    private int gpsY;
    private int gpsZ;

    public AirCannonScreen(AirCannonMenu container, Inventory inventoryPlayer, Component displayName) {
        super(container, inventoryPlayer, displayName);

        gpsX = te.gpsX;
        gpsY = te.gpsY;
        gpsZ = te.gpsZ;
    }

    @Override
    public void init() {
        super.init();

        statusStat = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.airCannon.status"),
                new ItemStack(ModBlocks.AIR_CANNON.get()), 0xFFFF8000, false);

        strengthTab = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.airCannon.force", te.forceMult),
                new ItemStack(ModItems.AIR_CANISTER.get()), 0xFF2080FF, false);
        strengthTab.setMinimumExpandedDimensions(85, 40);
        strengthTab.addSubWidget(new WidgetButtonExtended(16, 16, 20, 20, "--").withTag("--"));
        strengthTab.addSubWidget(new WidgetButtonExtended(38, 16, 20, 20, "-").withTag("-"));
        strengthTab.addSubWidget(new WidgetButtonExtended(60, 16, 20, 20, "+").withTag("+"));
        strengthTab.addSubWidget(new WidgetButtonExtended(82, 16, 20, 20, "++").withTag("++"));

        addLabel(Component.literal("GPS"),  leftPos + 50, topPos + 20);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_CANNON;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        statusStat.setText(getStatusText());
        strengthTab.setMessage(xlate("pneumaticcraft.gui.tab.info.airCannon.force", te.forceMult));

        if (gpsX != te.gpsX || gpsY != te.gpsY || gpsZ != te.gpsZ) {
            gpsX = te.gpsX;
            gpsY = te.gpsY;
            gpsZ = te.gpsZ;
            statusStat.openStat();
        }
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();
        if (te.gpsX != 0 || te.gpsY != 0 || te.gpsZ != 0) {
            text.add(xlate("pneumaticcraft.gui.tab.info.airCannon.coord", te.gpsX, te.gpsY, te.gpsZ).withStyle(ChatFormatting.BLACK));
        } else {
            text.add(xlate("pneumaticcraft.gui.tab.info.airCannon.no_coord").withStyle(ChatFormatting.BLACK));
        }
        text.add(xlate("pneumaticcraft.gui.tab.info.airCannon.heading", Math.round(te.rotationAngle)).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.info.airCannon.height", Math.round(te.heightAngle)).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.info.airCannon.range", Math.round(te.getForce() * 25F)).withStyle(ChatFormatting.BLACK));
        return text;
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);

        if (te.hasNoConnectedAirHandlers()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.airLeak"));
        }
        if (menu.slots.get(5).getItem().isEmpty() && te.getUpgrades(ModUpgrades.ENTITY_TRACKER.get()) == 0) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.air_cannon.no_items"));
        }
        if (!te.hasCoordinate()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.air_cannon.no_coordinate"));
        } else if (!te.coordWithinReach) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.air_cannon.out_of_range"));
        } else if (te.getRedstoneMode() == 0 && !te.doneTurning) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.air_cannon.still_turning"));
        } else if (te.getRedstoneMode() == 2 && !te.insertingInventoryHasSpace) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.air_cannon.inv_space"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        // nothing: override default redstone warnings
    }

    @Override
    protected void addInformation(List<Component> curInfo) {
        super.addInformation(curInfo);
        if (curInfo.isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.tooltip.apply_redstone"));
        }
    }
}
