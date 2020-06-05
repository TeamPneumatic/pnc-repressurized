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

        String invisibleText = I18n.format("gui.logistics_frame.invisible");
        WidgetCheckBox invisible;
        addButton(invisible = new WidgetCheckBox(guiLeft + xSize - 18 - font.getStringWidth(invisibleText), guiTop + 16, 0xFF404040, invisibleText, b -> {
            logistics.setSemiblockInvisible(b.checked);
            syncToServer();
        }).setChecked(logistics.isSemiblockInvisible()));

        invisible.setTooltip(PneumaticCraftUtils.splitString(
                I18n.format("gui.logistics_frame.invisible.tooltip"), 40));

        addButton(itemLabel = new WidgetLabel(guiLeft + 8, guiTop + 18, ""));
        addButton(fluidLabel = new WidgetLabel(guiLeft + 8, guiTop + 90, ""));
        updateLabels();

        IntStream.range(0, EntityLogisticsFrame.FLUID_FILTER_SLOTS).forEach(i -> {
            FluidStack stack = logistics.getFluidFilter(i);
            PointXY p = getFluidSlotPos(i);
            fluidWidgets.add(new WidgetFluidStack(p.x, p.y, stack.copy(), w -> fluidClicked((WidgetFluidStack) w, i)));
        });
        fluidWidgets.forEach(this::addButton);

        addInfoTab(I18n.format("gui.tooltip.item.pneumaticcraft." + logistics.getId().getPath()));
        addFilterTab();
        if (!container.isItemContainer()) {
            addFacingTab();
        }
        addJeiFilterInfoTab();
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
        String s = logistics.isWhiteList() ? "" : TextFormatting.STRIKETHROUGH.toString();
        itemLabel.setMessage(s + I18n.format(String.format("gui.%s.itemFilters", logistics.getId().getPath())));
        fluidLabel.setMessage(s + I18n.format(String.format("gui.%s.fluidFilters", logistics.getId().getPath())));
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
        WidgetAnimatedStat filterTab = addAnimatedStat("gui.logistics_frame.filter_settings",
                new ItemStack(Blocks.COBWEB), 0xFF106010, false);
        filterTab.addPadding(logistics.supportsBlacklisting() ? 8 : 6, 28);

        WidgetCheckBox matchDurability = new WidgetCheckBox(5, 20, 0xFFFFFFFF, I18n.format("gui.logistics_frame.matchDurability"), b -> {
            logistics.setMatchDurability(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitString(I18n.format("gui.logistics_frame.matchDurability.tooltip"), 40))
                .setChecked(logistics.isMatchDurability());
        filterTab.addSubWidget(matchDurability);

        WidgetCheckBox matchNBT = new WidgetCheckBox(5, 36, 0xFFFFFFFF, I18n.format("gui.logistics_frame.matchNBT"), b -> {
            logistics.setMatchNBT(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitString(I18n.format("gui.logistics_frame.matchNBT.tooltip"), 40))
                .setChecked(logistics.isMatchNBT());
        filterTab.addSubWidget(matchNBT);

        WidgetCheckBox matchModId = new WidgetCheckBox(5, 52, 0xFFFFFFFF, I18n.format("gui.logistics_frame.matchModId"), b -> {
            logistics.setMatchModId(b.checked);
            syncToServer();
        })
                .setTooltip(PneumaticCraftUtils.splitString(I18n.format("gui.logistics_frame.matchModId.tooltip"), 40))
                .setChecked(logistics.isMatchModId());
        filterTab.addSubWidget(matchModId);

        if (logistics.supportsBlacklisting()) {
            WidgetCheckBox whitelist = new WidgetCheckBox(5, 73, 0xFFFFFFFF, I18n.format("gui.logistics_frame.whitelist"), b -> {
                logistics.setWhiteList(b.checked);
                updateLabels();
                syncToServer();
            }).setChecked(logistics.isWhiteList());
            filterTab.addSubWidget(whitelist);
        }
    }

    private void addFacingTab() {
        facingTab = addAnimatedStat("", new ItemStack(Items.MAP), 0xFFC0C0C0, false);
        facingTab.addPadding(8, 18);

        facingTab.addSubWidget(facingButtons[0] = new WidgetButtonExtended(15, 62, 20, 20, "D",
                b -> setFace(Direction.DOWN)));
        facingTab.addSubWidget(facingButtons[1] = new WidgetButtonExtended(15, 20, 20, 20, "U",
                b -> setFace(Direction.UP)));
        facingTab.addSubWidget(facingButtons[2] = new WidgetButtonExtended(36, 20, 20, 20, "N",
                b -> setFace(Direction.NORTH)));
        facingTab.addSubWidget(facingButtons[3] = new WidgetButtonExtended(36, 62, 20, 20, "S",
                b -> setFace(Direction.SOUTH)));
        facingTab.addSubWidget(facingButtons[4] = new WidgetButtonExtended(15, 41, 20, 20, "W",
                b -> setFace(Direction.WEST)));
        facingTab.addSubWidget(facingButtons[5] = new WidgetButtonExtended(57, 41, 20, 20, "E",
                b -> setFace(Direction.EAST)));

        facingTab.addSubWidget(new WidgetButtonExtended(36, 41, 20, 20, "")
                .setTooltipText(PneumaticCraftUtils.splitString(I18n.format("gui.logistics_frame.facing.tooltip")))
                .setRenderedIcon(Textures.GUI_INFO_LOCATION)
                .setVisible(false)
        );
        updateFacing();
    }

    private void updateFacing() {
        String s = logistics.getFacing() == null ? "-" : ClientUtils.translateDirection(logistics.getFacing());
        facingTab.setTitle(I18n.format("gui.logistics_frame.facing", s));
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
