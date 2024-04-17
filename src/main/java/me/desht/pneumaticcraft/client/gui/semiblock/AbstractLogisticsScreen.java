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

package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftContainerScreen;
import me.desht.pneumaticcraft.client.gui.ItemSearcherScreen;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.entity.semiblock.LogisticsRequesterEntity;
import me.desht.pneumaticcraft.common.inventory.LogisticsMenu;
import me.desht.pneumaticcraft.common.inventory.slot.PhantomSlot;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AbstractLogisticsScreen<L extends AbstractLogisticsFrameEntity> extends AbstractPneumaticCraftContainerScreen<LogisticsMenu, AbstractPneumaticCraftBlockEntity> {
    protected final L logistics;
    private ItemSearcherScreen itemSearchGui;
    private LogisticsLiquidFilterScreen fluidSearchGui;
    private int editingSlot; // used for both fluid & item search.
    private WidgetLabel itemLabel;
    private WidgetLabel fluidLabel;
    private WidgetButtonExtended itemWhitelist;
    private WidgetButtonExtended fluidWhitelist;
    private final List<WidgetFluidStack> fluidWidgets = new ArrayList<>();
    private WidgetTextFieldNumber minItemsField;
    private WidgetTextFieldNumber minFluidField;

    public AbstractLogisticsScreen(LogisticsMenu menu, Inventory inv, Component displayString) {
        super(menu, inv, displayString);

        this.logistics = (L) menu.logistics;
        imageHeight = 216;
    }

    @Override
    public void init() {
        super.init();

        if (itemSearchGui != null) {
            updateItemFilter(editingSlot, itemSearchGui.getSearchStack());
            itemSearchGui = null;
        }

        if (fluidSearchGui != null && fluidSearchGui.getFilter() != null) {
            updateFluidFilter(editingSlot, new FluidStack(fluidSearchGui.getFilter(), 1000));
            fluidSearchGui = null;
        }

        Component invisibleText = xlate("pneumaticcraft.gui.logistics_frame.invisible");
        WidgetCheckBox invisible;
        addRenderableWidget(invisible = new WidgetCheckBox(leftPos + imageWidth - 20 - font.width(invisibleText), topPos + 17, 0xFF404040, invisibleText, b -> {
            logistics.setSemiblockInvisible(b.checked);
            syncToServer();
        }).setChecked(logistics.isSemiblockInvisible()));

        invisible.setTooltipKey("pneumaticcraft.gui.logistics_frame.invisible.tooltip");

        addRenderableWidget(itemWhitelist = new WidgetButtonExtended(leftPos + 5, topPos + 16, 12, 12, Component.empty(), b -> {
            logistics.setItemWhiteList(!logistics.isItemWhiteList());
            updateLabels();
            syncToServer();
        }).setVisible(false).setInvisibleHoverColor(0x80808080)).setTooltipKey("pneumaticcraft.gui.logistics_frame.itemWhitelist.tooltip");
        itemWhitelist.visible = logistics.supportsBlacklisting();
        addRenderableWidget(fluidWhitelist = new WidgetButtonExtended(leftPos + 5, topPos + 88, 12, 12, Component.empty(), b -> {
            logistics.setFluidWhiteList(!logistics.isFluidWhiteList());
            updateLabels();
            syncToServer();
        }).setVisible(false).setInvisibleHoverColor(0x80808080)).setTooltipKey("pneumaticcraft.gui.logistics_frame.fluidWhitelist.tooltip");;
        fluidWhitelist.visible = logistics.supportsBlacklisting();
        int xOff = logistics.supportsBlacklisting() ? 13 : 0;
        addRenderableWidget(itemLabel = new WidgetLabel(leftPos + 5 + xOff, topPos + 18, Component.empty()) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (itemWhitelist.visible) itemWhitelist.onClick(mouseX, mouseY);
            }
        });
        addRenderableWidget(fluidLabel = new WidgetLabel(leftPos + 5 + xOff, topPos + 90, Component.empty()) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                if (fluidWhitelist.visible) fluidWhitelist.onClick(mouseX, mouseY);
            }
        });
        updateLabels();

        fluidWidgets.clear();
        IntStream.range(0, AbstractLogisticsFrameEntity.FLUID_FILTER_SLOTS).forEach(i -> {
            FluidStack stack = logistics.getFluidFilter(i);
            PointXY p = getFluidSlotPos(i);
            WidgetFluidStack widgetFluidStack = new WidgetFluidStack(p.x(), p.y(), stack.copy(), w -> fluidClicked((WidgetFluidStack) w, i));
            if (logistics instanceof LogisticsRequesterEntity) {
                widgetFluidStack.setAdjustable();
            }
            fluidWidgets.add(widgetFluidStack);
        });
        fluidWidgets.forEach(this::addRenderableWidget);

        addInfoTab(GuiUtils.xlateAndSplit("gui.tooltip.item.pneumaticcraft." + logistics.getSemiblockId().getPath()));
        addFilterTab();
        addJeiFilterInfoTab();

        if (logistics instanceof ISpecificRequester) {
            addMinOrderSizeTab();
        }
    }

    private void addMinOrderSizeTab() {
        WidgetAnimatedStat minAmountStat = addAnimatedStat(xlate("pneumaticcraft.gui.logistics_frame.min_amount"), new ItemStack(Blocks.CHEST), 0xFFC0C080, false);

        WidgetLabel minItemsLabel = new WidgetLabel(5, 20, xlate("pneumaticcraft.gui.logistics_frame.min_items"));
        minItemsLabel.setTooltipText(xlate("pneumaticcraft.gui.logistics_frame.min_items.tooltip"));
        minAmountStat.addSubWidget(minItemsLabel);
        minItemsField = new WidgetTextFieldNumber(font, 5, 30, 30, 12)
                .setRange(1, 64)
                .setAdjustments(1, 10)
                .setValue(((ISpecificRequester) logistics).getMinItemOrderSize());
        minItemsField.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minItemsField);

        WidgetLabel minFluidLabel = new WidgetLabel(5, 47, xlate("pneumaticcraft.gui.logistics_frame.min_fluid"));
        minFluidLabel.setTooltipText(xlate("pneumaticcraft.gui.logistics_frame.min_fluid.tooltip"));
        minAmountStat.addSubWidget(minFluidLabel);
        minFluidField = new WidgetTextFieldNumber(font, 5, 57, 50, 12)
                .setRange(1, 16000)
                .setAdjustments(100, 1000)
                .setValue(((ISpecificRequester) logistics).getMinFluidOrderSize());
        minFluidField.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minFluidField);

        int w = Math.max(minItemsLabel.getWidth(), minFluidLabel.getWidth());
        minAmountStat.setMinimumExpandedDimensions(w, 75);
    }

    @Override
    protected void doDelayedAction() {
        if (logistics instanceof ISpecificRequester s) {
            s.setMinItemOrderSize(minItemsField.getIntValue());
            s.setMinFluidOrderSize(minFluidField.getIntValue());
            syncToServer();
        }
    }

    public void updateItemFilter(int slot, ItemStack stack) {
        menu.getSlot(slot).set(stack);
        logistics.setItemFilter(slot, stack);
        syncToServer();
    }

    public void updateFluidFilter(int slot, FluidStack stack) {
        logistics.setFluidFilter(slot, stack);
        if (!fluidWidgets.isEmpty()) fluidWidgets.get(slot).setFluidStack(stack);
        syncToServer();
    }

    public PointXY getFluidSlotPos(int slot) {
        return new PointXY(leftPos + slot * 18 + 8, topPos + 101);
    }

    private void updateLabels() {
        itemLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame." + (logistics.isItemWhiteList() ? "itemWhitelist" : "itemBlacklist")));
        fluidLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame." + (logistics.isFluidWhiteList() ? "fluidWhitelist" : "fluidBlacklist")));
        itemWhitelist.setRenderedIcon(logistics.isItemWhiteList() ? Textures.GUI_WHITELIST : Textures.GUI_BLACKLIST);
        fluidWhitelist.setRenderedIcon(logistics.isFluidWhiteList() ? Textures.GUI_WHITELIST : Textures.GUI_BLACKLIST);
    }

    private void syncToServer() {
        NetworkHandler.sendToServer(PacketSyncSemiblock.create(logistics, menu.isItemContainer()));
    }

    private void fluidClicked(WidgetFluidStack widget, int idx) {
        FluidStack stack = logistics.getFluidFilter(idx);
        if (!stack.isEmpty()) {
            logistics.setFluidFilter(idx, FluidStack.EMPTY);
            widget.setFluid(Fluids.EMPTY);
            syncToServer();
            return;
        } else if (IOHelper.getFluidHandlerForItem(menu.getCarried()).isPresent()) {
            FluidStack f = IOHelper.getFluidHandlerForItem(menu.getCarried()).orElseThrow().getFluidInTank(0);
            logistics.setFluidFilter(idx, f.isEmpty() ? FluidStack.EMPTY : new FluidStack(f, 1000));
            widget.setFluid(f.getFluid());
            syncToServer();
            return;
        }

        fluidSearchGui = new LogisticsLiquidFilterScreen(this);
        editingSlot = idx;
        minecraft.setScreen(fluidSearchGui);
    }

    private void addFilterTab() {
        WidgetAnimatedStat filterTab = addAnimatedStat(xlate("pneumaticcraft.gui.logistics_frame.filter_settings"),
                RL("textures/gui/icon/gui_filter.png"), 0xFF106010, false);
        filterTab.setMinimumExpandedDimensions(80, 65);

        WidgetCheckBox matchDurability = new WidgetCheckBox(5, 20, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchDurability"), b -> {
            logistics.setMatchDurability(b.checked);
            syncToServer();
        })
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchDurability.tooltip")
                .setChecked(logistics.isMatchDurability());
        filterTab.addSubWidget(matchDurability);

        WidgetCheckBox matchNBT = new WidgetCheckBox(5, 36, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchNBT"), b -> {
            logistics.setMatchNBT(b.checked);
            syncToServer();
        })
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchNBT.tooltip")
                .setChecked(logistics.isMatchNBT());
        filterTab.addSubWidget(matchNBT);

        WidgetCheckBox matchModId = new WidgetCheckBox(5, 52, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchModId"), b -> {
            logistics.setMatchModId(b.checked);
            syncToServer();
        })
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchModId.tooltip")
                .setChecked(logistics.isMatchModId());
        filterTab.addSubWidget(matchModId);
    }

    @Override
    protected OptionalInt getBackgroundTint() {
        if (!ConfigHelper.client().general.logisticsGuiTint.get()) return super.getBackgroundTint();

        int c = logistics.getColor();
        // desaturate; this is a background colour...
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.2f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return OptionalInt.of(color.getRGB());
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LOGISTICS_REQUESTER;
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int clickedButton, ClickType clickType) {
        if (slot instanceof PhantomSlot
                && menu.getCarried().isEmpty()
                && !slot.hasItem()
                && (clickedButton == 0 || clickedButton == 1)) {
            editingSlot = slot.getSlotIndex();
            ClientUtils.openContainerGui(ModMenuTypes.ITEM_SEARCHER.get(), Component.literal("Searcher"));
            if (minecraft.screen instanceof ItemSearcherScreen) {
                itemSearchGui = (ItemSearcherScreen) minecraft.screen;
            }
        } else {
            super.slotClicked(slot, slotId, clickedButton, clickType);
        }
    }
}
