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
import me.desht.pneumaticcraft.common.semiblock.ISpecificProvider;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    protected WidgetLabel itemLabel;
    protected WidgetLabel fluidLabel;
    private WidgetButtonExtended itemWhitelist;
    private WidgetButtonExtended fluidWhitelist;
    private final List<WidgetFluidStack> fluidWidgets = new ArrayList<>();
    private LimitFields minFields, stockFields;

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
        }).setVisible(false).setInvisibleHoverColor(0x80808080)).setTooltipKey("pneumaticcraft.gui.logistics_frame.fluidWhitelist.tooltip");
        fluidWhitelist.visible = logistics.supportsBlacklisting();
        int xOff = logistics.supportsBlacklisting() ? 13 : 0;
        addRenderableWidget(itemLabel = new WidgetLabel(leftPos + 5 + xOff, topPos + 18, Component.empty()) {
            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if (itemWhitelist.visible) itemWhitelist.onClick(mouseX, mouseY, button);
            }
        });
        addRenderableWidget(fluidLabel = new WidgetLabel(leftPos + 5 + xOff, topPos + 90, Component.empty()) {
            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                if (fluidWhitelist.visible) fluidWhitelist.onClick(mouseX, mouseY, button);
            }
        });
        updateLabels();

        fluidWidgets.clear();
        IntStream.range(0, AbstractLogisticsFrameEntity.FLUID_FILTER_SLOTS).forEach(i -> {
            FluidStack stack = logistics.getFluidFilter(i);
            PointXY p = getFluidSlotPos(i);
            WidgetFluidStack widgetFluidStack = new WidgetFluidStack(p.x(), p.y(), stack.copy(), b -> fluidClicked(b, i));
            if (logistics instanceof LogisticsRequesterEntity) {
                widgetFluidStack.setAdjustable();
            }
            fluidWidgets.add(widgetFluidStack);
        });
        fluidWidgets.forEach(this::addRenderableWidget);

        addInfoTab(GuiUtils.xlateAndSplit("gui.tooltip.item.pneumaticcraft." + logistics.getSemiblockId().getPath()));
        addFilterTab();
        addJeiFilterInfoTab();

        if (logistics instanceof ISpecificRequester sr) {
            minFields = addLimitsTab("min", Items.CHEST, sr.getMinItemOrderSize(), sr.getMinFluidOrderSize(), 1);
        }
        if (logistics instanceof ISpecificProvider sp) {
            stockFields = addLimitsTab("stock", Items.BARREL, sp.getKeepItemsStocked(), sp.getKeepFluidStocked(), 0);
        }
    }

    private LimitFields addLimitsTab(String what, Item iconItem, int itemAmount, int fluidAmount, int min) {
        String baseKey = "pneumaticcraft.gui.logistics_frame." + what;
        var stat = addAnimatedStat(
                xlate(baseKey + "_amount"), iconItem.getDefaultInstance(), 0xFFC0C080, false
        );

        var itemLabel = new WidgetLabel(5, 20, xlate(baseKey + "_items"))
                .setTooltipText(xlate(baseKey + "_items.tooltip"));
        var itemField = new WidgetTextFieldNumber(font, 5, 30, 30, 12)
                .setRange(min, 64)
                .setAdjustments(1, 10)
                .setValue(itemAmount);
        itemField.setResponder(s -> sendDelayed(8));

        var fluidLabel = new WidgetLabel(5, 47, xlate(baseKey + "_fluid"))
                .setTooltipText(xlate(baseKey + "_fluid.tooltip"));
        var fluidField = new WidgetTextFieldNumber(font, 5, 57, 50, 12)
                .setRange(min, 16000)
                .setAdjustments(100, 1000)
                .setValue(fluidAmount);
        fluidField.setResponder(s -> sendDelayed(8));

        stat.addSubWidget(itemLabel, itemField, fluidLabel, fluidField);
        int w = Math.max(itemLabel.getWidth(), fluidLabel.getWidth());
        stat.setMinimumExpandedDimensions(w, 75);

        return new LimitFields(itemField, fluidField);
    }

    @Override
    protected void doDelayedAction() {
        boolean sync = false;
        if (logistics instanceof ISpecificRequester s) {
            s.setMinItemOrderSize(minFields.items.getIntValue());
            s.setMinFluidOrderSize(minFields.fluids.getIntValue());
            sync = true;
        }
        if (logistics instanceof ISpecificProvider p) {
            p.setKeepItemsStocked(stockFields.items.getIntValue());
            p.setKeepFluidsStocked(stockFields.fluids.getIntValue());
            sync = true;
        }
        if (sync) syncToServer();
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

    protected void updateLabels() {
        itemLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame." + (logistics.isItemWhiteList() ? "itemWhitelist" : "itemBlacklist")));
        fluidLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame." + (logistics.isFluidWhiteList() ? "fluidWhitelist" : "fluidBlacklist")));
        itemWhitelist.setRenderedIcon(logistics.isItemWhiteList() ? Textures.GUI_WHITELIST : Textures.GUI_BLACKLIST);
        fluidWhitelist.setRenderedIcon(logistics.isFluidWhiteList() ? Textures.GUI_WHITELIST : Textures.GUI_BLACKLIST);
    }

    private void syncToServer() {
        NetworkHandler.sendToServer(PacketSyncSemiblock.create(logistics, menu.isItemContainer(), registryAccess()));
    }

    private void fluidClicked(WidgetFluidStack widget, int idx) {
        FluidStack stack = logistics.getFluidFilter(idx);
        if (!stack.isEmpty()) {
            logistics.setFluidFilter(idx, widget.getFluidStack().copy());
            syncToServer();
            return;
        } else if (IOHelper.getFluidHandlerForItem(menu.getCarried()).isPresent()) {
            FluidStack f = IOHelper.getFluidHandlerForItem(menu.getCarried()).orElseThrow().getFluidInTank(0);
            logistics.setFluidFilter(idx, f.isEmpty() ? FluidStack.EMPTY : f.copyWithAmount(1000));
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

        WidgetCheckBox matchComponents = new WidgetCheckBox(5, 36, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchComponents"), b -> {
            logistics.setMatchComponents(b.checked);
            syncToServer();
        })
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchComponents.tooltip")
                .setChecked(logistics.isMatchComponents());

        WidgetCheckBox matchModId = new WidgetCheckBox(5, 52, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchModId"), b -> {
            logistics.setMatchModId(b.checked);
            syncToServer();
        })
                .setTooltipKey("pneumaticcraft.gui.logistics_frame.matchModId.tooltip")
                .setChecked(logistics.isMatchModId());

        filterTab.addSubWidget(matchDurability, matchComponents, matchModId);
    }

    @Override
    protected OptionalInt getBackgroundTint() {
        if (!ConfigHelper.client().general.logisticsGuiTint.get()) return super.getBackgroundTint();

        int c = logistics.getColor();
        // desaturate; this is a background colour...
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.2f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return OptionalInt.of(color.getARGB());
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

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        Slot slot = findSlot(pMouseX, pMouseY);
        if (slot != null) {
            if (pScrollY > 0) {
                if (Screen.hasShiftDown()) {
                    slotClicked(slot, slot.index, 1, ClickType.QUICK_MOVE);
                } else {
                    slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                }
            } else if (pScrollY < 0) {
                if (Screen.hasShiftDown()) {
                    slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                } else {
                    slotClicked(slot, slot.index, 0, ClickType.PICKUP);
                }
            }
            return true;
        }

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    private Slot findSlot(double pMouseX, double pMouseY) {
        return menu.slots.stream()
                .filter(slot -> isHovering(slot.x, slot.y, 16, 16, pMouseX, pMouseY) && slot.isActive())
                .findFirst()
                .orElse(null);

    }

    private record LimitFields(WidgetTextFieldNumber items, WidgetTextFieldNumber fluids) {
    }
}
