package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncSemiblock;
import me.desht.pneumaticcraft.common.semiblock.ISpecificRequester;
import me.desht.pneumaticcraft.common.tileentity.TileEntityBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLogisticsBase<L extends EntityLogisticsFrame> extends GuiPneumaticContainerBase<ContainerLogistics, TileEntityBase> {
    protected final L logistics;
    private GuiItemSearcher itemSearchGui;
    private GuiLogisticsLiquidFilter fluidSearchGui;
    private int editingSlot; // used for both fluid & item search.
    private final WidgetButtonExtended[] facingButtons = new WidgetButtonExtended[6];
    private WidgetAnimatedStat facingTab;
    private WidgetLabel itemLabel;
    private WidgetLabel fluidLabel;
    private final List<WidgetFluidStack> fluidWidgets = new ArrayList<>();
    private WidgetTextFieldNumber minItemsField;
    private WidgetTextFieldNumber minFluidField;

    public GuiLogisticsBase(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        this.logistics = (L) container.logistics;
        ySize = 216;
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

        ITextComponent invisibleText = xlate("pneumaticcraft.gui.logistics_frame.invisible");
        WidgetCheckBox invisible;
        addButton(invisible = new WidgetCheckBox(guiLeft + xSize - 18 - font.func_238414_a_(invisibleText), guiTop + 16, 0xFF404040, invisibleText, b -> {
            logistics.setSemiblockInvisible(b.checked);
            syncToServer();
        }).setChecked(logistics.isSemiblockInvisible()));

        invisible.setTooltip(PneumaticCraftUtils.splitStringComponent(
                I18n.format("pneumaticcraft.gui.logistics_frame.invisible.tooltip")));

        addButton(itemLabel = new WidgetLabel(guiLeft + 8, guiTop + 18, StringTextComponent.EMPTY));
        addButton(fluidLabel = new WidgetLabel(guiLeft + 8, guiTop + 90, StringTextComponent.EMPTY));
        updateLabels();

        fluidWidgets.clear();
        IntStream.range(0, EntityLogisticsFrame.FLUID_FILTER_SLOTS).forEach(i -> {
            FluidStack stack = logistics.getFluidFilter(i);
            PointXY p = getFluidSlotPos(i);
            fluidWidgets.add(new WidgetFluidStack(p.x, p.y, stack.copy(), w -> fluidClicked((WidgetFluidStack) w, i)));
        });
        fluidWidgets.forEach(this::addButton);

        addInfoTab(I18n.format("pneumaticcraft.gui.tooltip.item.pneumaticcraft." + logistics.getId().getPath()));
        addFilterTab();
        if (!container.isItemContainer()) {
            addFacingTab();
        }
        addJeiFilterInfoTab();

        if (logistics instanceof ISpecificRequester) {
            addMinOrderSizeTab();
        }
    }

    private void addMinOrderSizeTab() {
        WidgetAnimatedStat minAmountStat = addAnimatedStat(xlate("pneumaticcraft.gui.logistics_frame.min_amount"), new ItemStack(Blocks.CHEST), 0xFFC0C080, false);
        minAmountStat.addPadding(7, 21);

        WidgetLabel minItemsLabel = new WidgetLabel(5, 20, xlate("pneumaticcraft.gui.logistics_frame.min_items"));
        minItemsLabel.setTooltip(xlate("pneumaticcraft.gui.logistics_frame.min_items.tooltip"));
        minAmountStat.addSubWidget(minItemsLabel);
        minItemsField = new WidgetTextFieldNumber(font, 5, 30, 30, 12)
                .setRange(1, 64)
                .setValue(((ISpecificRequester) logistics).getMinItemOrderSize());
        minItemsField.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minItemsField);

        WidgetLabel minFluidLabel = new WidgetLabel(5, 47, xlate("pneumaticcraft.gui.logistics_frame.min_fluid"));
        minFluidLabel.setTooltip(xlate("pneumaticcraft.gui.logistics_frame.min_fluid.tooltip"));
        minAmountStat.addSubWidget(minFluidLabel);
        minFluidField = new WidgetTextFieldNumber(font, 5, 57, 50, 12)
                .setRange(1, 16000)
                .setValue(((ISpecificRequester) logistics).getMinFluidOrderSize());
        minFluidField.setResponder(s -> sendDelayed(8));
        minAmountStat.addSubWidget(minFluidField);
    }

    @Override
    protected void doDelayedAction() {
        if (logistics instanceof ISpecificRequester) {
            ((ISpecificRequester) logistics).setMinItemOrderSize(minItemsField.getValue());
            ((ISpecificRequester) logistics).setMinFluidOrderSize(minFluidField.getValue());
            syncToServer();
        }
    }

    public void updateItemFilter(int slot, ItemStack stack) {
        container.getSlot(slot).putStack(stack);
        logistics.setItemFilter(slot, stack);
        syncToServer();
    }

    public void updateFluidFilter(int slot, FluidStack stack) {
        logistics.setFluidFilter(slot, stack);
        if (!fluidWidgets.isEmpty()) fluidWidgets.get(slot).setFluidStack(stack);
        syncToServer();
    }

    public PointXY getFluidSlotPos(int slot) {
        return new PointXY(guiLeft + slot * 18 + 8, guiTop + 101);
    }

    private void updateLabels() {
        TextFormatting s = logistics.isWhiteList() ? TextFormatting.RESET : TextFormatting.STRIKETHROUGH;
        itemLabel.setMessage(xlate(String.format("pneumaticcraft.gui.%s.itemFilters", logistics.getId().getPath())).mergeStyle(s));
        fluidLabel.setMessage(xlate(String.format("pneumaticcraft.gui.%s.fluidFilters", logistics.getId().getPath())).mergeStyle(s));
    }

    private void syncToServer() {
        NetworkHandler.sendToServer(new PacketSyncSemiblock(logistics));
    }

    private void fluidClicked(WidgetFluidStack widget, int idx) {
        FluidStack stack = logistics.getFluidFilter(idx);
        if (!stack.isEmpty()) {
            logistics.setFluidFilter(idx, widget.getFluidStack().copy());
            syncToServer();
        } else {
            fluidSearchGui = new GuiLogisticsLiquidFilter(this);
            editingSlot = idx;
            minecraft.displayGuiScreen(fluidSearchGui);
        }
    }

    private void addFilterTab() {
        WidgetAnimatedStat filterTab = addAnimatedStat(xlate("pneumaticcraft.gui.logistics_frame.filter_settings"),
                new ItemStack(Blocks.COBWEB), 0xFF106010, false);
        filterTab.addPadding(logistics.supportsBlacklisting() ? 8 : 6, 28);

        WidgetCheckBox matchDurability = new WidgetCheckBox(5, 20, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchDurability"), b -> {
            logistics.setMatchDurability(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.logistics_frame.matchDurability.tooltip")))
                .setChecked(logistics.isMatchDurability());
        filterTab.addSubWidget(matchDurability);

        WidgetCheckBox matchNBT = new WidgetCheckBox(5, 36, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchNBT"), b -> {
            logistics.setMatchNBT(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.logistics_frame.matchNBT.tooltip")))
                .setChecked(logistics.isMatchNBT());
        filterTab.addSubWidget(matchNBT);

        WidgetCheckBox matchModId = new WidgetCheckBox(5, 52, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.matchModId"), b -> {
            logistics.setMatchModId(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.logistics_frame.matchModId.tooltip")))
                .setChecked(logistics.isMatchModId());
        filterTab.addSubWidget(matchModId);

        if (logistics.supportsBlacklisting()) {
            WidgetCheckBox whitelist = new WidgetCheckBox(5, 73, 0xFFFFFFFF, xlate("pneumaticcraft.gui.logistics_frame.whitelist"), b -> {
                logistics.setWhiteList(b.checked);
                updateLabels();
                syncToServer();
            }).setChecked(logistics.isWhiteList());
            filterTab.addSubWidget(whitelist);
        }
    }

    private void addFacingTab() {
        facingTab = addAnimatedStat(StringTextComponent.EMPTY, new ItemStack(Items.MAP), 0xFFC0C0C0, false);
        facingTab.addPadding(8, 18);

        addDirButton(0, 15, 62);
        addDirButton(1, 15, 20);
        addDirButton(2, 36, 20);
        addDirButton(3, 36, 62);
        addDirButton(4, 15, 41);
        addDirButton(5, 57, 41);

        facingTab.addSubWidget(new WidgetButtonExtended(36, 41, 20, 20, StringTextComponent.EMPTY)
                .setTooltipText(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.logistics_frame.facing.tooltip")))
                .setRenderedIcon(Textures.GUI_INFO_LOCATION)
                .setVisible(false)
        );
        updateFacing();
    }

    private void addDirButton(int i, int x, int y) {
        Direction dir = Direction.byIndex(i);
        String label = dir.toString().substring(0, 1).toUpperCase();
        facingTab.addSubWidget(facingButtons[i] = new WidgetButtonExtended(x, y, 20, 20, new StringTextComponent(label), b -> setFace(dir)));
    }

    private void updateFacing() {
        String s = logistics.getFacing() == null ? "-" : ClientUtils.translateDirection(logistics.getFacing());
        facingTab.setMessage(xlate("pneumaticcraft.gui.logistics_frame.facing", s));
        for (Direction face : Direction.values()) {
            facingButtons[face.getIndex()].active = face != logistics.getFacing();
        }
    }

    private void setFace(Direction face) {
        logistics.setFacing(face);
        syncToServer();
        updateFacing();
    }

    @Override
    protected int getBackgroundTint() {
        if (!PNCConfig.Client.logisticsGuiTint) return super.getBackgroundTint();

        int c = logistics.getColor();
        // desaturate; this is a background colour...
        float[] hsb = TintColor.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        TintColor color = TintColor.getHSBColor(hsb[0], hsb[1] * 0.2f, hsb[2]);
        if (hsb[2] < 0.7) color = color.brighter();
        return color.getRGB();
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
    protected void handleMouseClick(Slot slot, int slotId, int clickedButton, ClickType clickType) {
        if (slot instanceof SlotPhantom
                && minecraft.player.inventory.getItemStack().isEmpty()
                && !slot.getHasStack()
                && (clickedButton == 0 || clickedButton == 1)) {
            editingSlot = slot.getSlotIndex();
            ClientUtils.openContainerGui(ModContainers.ITEM_SEARCHER.get(), new StringTextComponent("Searcher"));
            if (minecraft.currentScreen instanceof GuiItemSearcher) {
                itemSearchGui = (GuiItemSearcher) minecraft.currentScreen;
            }
        } else {
            super.handleMouseClick(slot, slotId, clickedButton, clickType);
        }
    }
}
