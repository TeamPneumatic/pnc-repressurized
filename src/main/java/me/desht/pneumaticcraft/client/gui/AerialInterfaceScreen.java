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
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.XPFluidManager;
import me.desht.pneumaticcraft.common.block.entity.AerialInterfaceBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.AerialInterfaceBlockEntity.FeedMode;
import me.desht.pneumaticcraft.common.block.entity.AerialInterfaceBlockEntity.OperatingProblem;
import me.desht.pneumaticcraft.common.inventory.AerialInterfaceMenu;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AerialInterfaceScreen extends AbstractPneumaticCraftContainerScreen<AerialInterfaceMenu,AerialInterfaceBlockEntity> {
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[FeedMode.values().length];
    private WidgetButtonExtended xpButton;
    private WidgetAnimatedStat feedModeTab;

    public AerialInterfaceScreen(AerialInterfaceMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info.title"),
                Textures.GUI_BUILDCRAFT_ENERGY, 0xFFA02222, false)
                .setText(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.aerialInterface.interfacingRF.info"));

        IOHelper.getEnergyStorageForBlock(te).ifPresent(storage -> addRenderableWidget(new WidgetEnergy(leftPos + 20, topPos + 20, storage)));

        if (te.dispenserUpgradeInserted) {
            // Experience Tab
            List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
            if (!availableXp.isEmpty()) {
                WidgetAnimatedStat xpStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info.title"),
                        new ItemStack(Items.EXPERIENCE_BOTTLE), 0xFF55FF55, false);
                xpStat.setText(getLiquidXPText()).setForegroundColor(0xFF000000);
                xpButton = new WidgetButtonExtended(20, 15, 20, 20, Component.empty(), b -> {
                    te.curXPFluidIndex++;
                    if (te.curXPFluidIndex >= availableXp.size()) {
                        te.curXPFluidIndex = -1;
                    }
                    setupXPButton();
                }).withTag("xpType");
                setupXPButton();
                xpStat.addSubWidget(xpButton);
                xpStat.setReservedLines(3);
            }

            // Feeding Tab
            feedModeTab = addAnimatedStat(xlate(te.feedMode.getTranslationKey()), te.feedMode.getIconStack(), 0xFFFFA000, false);
            feedModeTab.setMinimumExpandedDimensions(80, 42);

            for (int i = 0; i < FeedMode.values().length; i++) {
                FeedMode mode = FeedMode.values()[i];
                List<Component> l = new ArrayList<>();
                l.add(xlate(mode.getTranslationKey()).withStyle(ChatFormatting.YELLOW));
                l.addAll(GuiUtils.xlateAndSplit(mode.getDescTranslationKey()));
                Component combined = l.stream().reduce((c1, c2) -> c1.copy().append("\n").append(c2)).orElse(Component.empty());

                WidgetButtonExtended button = new WidgetButtonExtended(5 + 25 * i, 20, 20, 20)
                        .withTag(mode.toString())
                        .setRenderStacks(mode.getIconStack());
                if (!combined.getString().isEmpty()) {
                    button.setTooltip(Tooltip.create(combined));
                }

                feedModeTab.addSubWidget(button);
                modeButtons[i] = button;
            }

            addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingFood"), new ItemStack(Items.BREAD), 0xFFA0A0A0, false)
                    .setText(xlate("pneumaticcraft.gui.tab.info.aerialInterface.removeDispenser")).setForegroundColor(0xFF000000);

        } else {
            addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.aerialInterface.interfacingItems"), new ItemStack(Blocks.CHEST), 0xFFA0A0A0, false)
                    .setText(xlate("pneumaticcraft.gui.tab.info.aerialInterface.insertDispenser")).setForegroundColor(0xFF000000);
            Arrays.fill(modeButtons, null);
        }
    }

    @Override
    protected boolean shouldAddSideConfigTabs() {
        return !te.dispenserUpgradeInserted;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (te.dispenserUpgradeInserted) {
            if (modeButtons[0] != null) {
                for (int i = 0; i < modeButtons.length; i++) {
                    modeButtons[i].active = te.feedMode != FeedMode.values()[i];
                }
                feedModeTab.setMessage(xlate(te.feedMode.getTranslationKey()));
            } else {
                refreshScreen();
            }
        } else if (modeButtons[0] != null) {
            refreshScreen();
        }
    }

    private void setupXPButton() {
        List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
        Fluid fluid = te.curXPFluidIndex >= 0 && te.curXPFluidIndex < availableXp.size() ?
                availableXp.get(te.curXPFluidIndex) : Fluids.EMPTY;
        if (fluid != Fluids.EMPTY) {
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            xpButton.setRenderStacks(FluidUtil.getFilledBucket(fluidStack));
            String modName = ModNameCache.getModName(fluid);
            xpButton.setTooltip(Tooltip.create(fluidStack.getDisplayName().copy().append("\n")
                    .append(Component.literal(modName).withStyle(ChatFormatting.ITALIC, ChatFormatting.BLUE))));
        } else {
            xpButton.setRenderStacks(new ItemStack(Items.BUCKET));
            xpButton.setTooltip(Tooltip.create(xlate("pneumaticcraft.gui.tooltip.aerial_interface.xpDisabled")));
        }
    }

    private List<Component> getLiquidXPText() {
        List<Component> liquidXpText = new ArrayList<>(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.aerialInterface.liquidXp.info"));
        liquidXpText.add(Component.empty());
        List<Fluid> availableXp = XPFluidManager.getInstance().getAvailableLiquidXPs();
        if (availableXp.isEmpty()) {
            liquidXpText.add(xlate("pneumaticcraft.gui.misc.none").withStyle(ChatFormatting.BLACK, ChatFormatting.ITALIC));
        } else {
            for (Fluid fluid : availableXp) {
                FluidStack stack = new FluidStack(fluid, 1000);
                String modName = ModNameCache.getModName(fluid);
                MutableComponent modNameText = Component.literal(" (" + modName + ")");
                liquidXpText.add(Symbols.bullet().withStyle(ChatFormatting.BLACK)
                        .append(stack.getDisplayName().copy().withStyle(ChatFormatting.BLACK))
                        .append(modNameText.withStyle(ChatFormatting.DARK_BLUE))
                );
            }
        }
        return liquidXpText;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_4UPGRADE_SLOTS;
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.getPressure() >= te.getMinWorkingPressure() && te.isConnectedToPlayer) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage",
                    PneumaticCraftUtils.roundNumberTo(PneumaticValues.USAGE_AERIAL_INTERFACE, 1)).withStyle(ChatFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);

        if (te.getPlayerName().isEmpty()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.aerialInterface.noPlayer"));
        } else if (!te.isConnectedToPlayer) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.aerialInterface.playerOffline", te.getPlayerName()));
        } else if (te.operatingProblem != OperatingProblem.OK) {
            textList.add(xlate(te.operatingProblem.getTranslationKey()));
        }
    }

    @Override
    protected void addInformation(List<Component> curInfo) {
        if (te.getPlayerName() != null && !te.getPlayerName().isEmpty()) {
            curInfo.add(xlate("pneumaticcraft.gui.tab.info.aerialInterface.linked", te.getPlayerName()));
        }
    }
}
