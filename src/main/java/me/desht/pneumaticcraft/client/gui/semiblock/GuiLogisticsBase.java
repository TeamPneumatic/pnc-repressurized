package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.GuiItemSearcher;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.widget.*;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModContainerTypes;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsFilterStack;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsFluidFilterStack;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.util.Arrays;

public class GuiLogisticsBase<L extends SemiBlockLogistics> extends GuiPneumaticContainerBase<ContainerLogistics,TileEntityBase> {
    protected final L logistics;
    private GuiItemSearcher searchGui;
    private GuiLogisticsLiquidFilter fluidSearchGui;
    private int editingSlot; //either fluid or item search.
    private WidgetCheckBox invisible;
    private WidgetCheckBox fuzzyDamage;
    private WidgetCheckBox fuzzyNBT;
    private WidgetCheckBox whitelist;
    private WidgetButtonExtended[] facingButtons = new WidgetButtonExtended[6];
    private WidgetAnimatedStat facingTab;

    public GuiLogisticsBase(ContainerLogistics container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        this.logistics = (L) container.logistics;
        ySize = 216;
    }

    @Override
    public void init() {
        super.init();

        if (searchGui != null) {
            container.getSlot(editingSlot).putStack(searchGui.getSearchStack());
            NetworkHandler.sendToServer(new PacketSetLogisticsFilterStack(logistics, searchGui.getSearchStack(), editingSlot));
            searchGui = null;
        }

        if (fluidSearchGui != null && fluidSearchGui.getFilter() != null) {
            FluidStack filter = new FluidStack(fluidSearchGui.getFilter(), 1000);
            logistics.setFilter(editingSlot, filter);
            NetworkHandler.sendToServer(new PacketSetLogisticsFluidFilterStack(logistics, filter, editingSlot));
            fluidSearchGui = null;
        }

        String invisibleText = I18n.format("gui.logistic_frame.invisible");
        addButton(invisible = new WidgetCheckBox(guiLeft + xSize - 15 - font.getStringWidth(invisibleText), guiTop + 6, 0xFF404040, invisibleText).withTag("invisible"));
        invisible.setTooltip(Arrays.asList(WordUtils.wrap(I18n.format("gui.logistic_frame.invisible.tooltip"), 40).split(System.getProperty("line.separator"))));

        addButton(new WidgetLabel(guiLeft + 8, guiTop + 18, I18n.format(String.format("gui.%s.filters", logistics.getId().getPath()))));
        addButton(new WidgetLabel(guiLeft + 8, guiTop + 90, I18n.format("gui.logistic_frame.liquid")));
        for (int i = 0; i < 9; i++) {
            final int idx = i;
            addButton(new WidgetFluidStack(guiLeft + i * 18 + 8, guiTop + 101, logistics.getTankFilter(i), b -> fluidClicked(idx)));
        }

        addInfoTab(I18n.format("gui.tab.info." + logistics.getId().getPath()));
        addFilterTab();
        if (!container.isItemContainer()) {
            addFacingTab();
        }
    }

    private void fluidClicked(int idx) {
        boolean leftClick = minecraft.mouseHelper.isLeftDown();
        boolean middleClick = minecraft.mouseHelper.middleDown;  // todo AT
        boolean shift = PneumaticCraftRepressurized.proxy.isSneakingInGui();
        IFluidTank tank = logistics.getTankFilter(idx);
        if (tank.getFluidAmount() > 0) {
            if (middleClick) {
                logistics.setFilter(idx, null);
            } else if (leftClick) {
                tank.drain(shift ? tank.getFluidAmount() / 2 : 1000, IFluidHandler.FluidAction.EXECUTE);
                if (tank.getFluidAmount() < 1000) {
                    tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                }
            } else {
                tank.fill(new FluidStack(tank.getFluid().getFluid(), shift ? tank.getFluidAmount() : 1000), IFluidHandler.FluidAction.EXECUTE);
            }
            NetworkHandler.sendToServer(new PacketSetLogisticsFluidFilterStack(logistics, tank.getFluid(), idx));
        } else {
            fluidSearchGui = new GuiLogisticsLiquidFilter(this);
            editingSlot = idx;
            minecraft.displayGuiScreen(fluidSearchGui);
        }
    }


    private void addFilterTab() {
        WidgetAnimatedStat filterTab = addAnimatedStat("gui.logistic_frame.filter_settings",
                new ItemStack(Blocks.COBWEB), 0xFF106010, false);
        filterTab.addPadding(logistics.supportsBlacklisting() ? 6 : 4, 26);
        fuzzyDamage = new WidgetCheckBox(5, 20, 0xFFFFFFFF, I18n.format("gui.logistic_frame.fuzzyDamage")).withTag("fuzzyDamage");
        filterTab.addSubWidget(fuzzyDamage);
        fuzzyNBT = new WidgetCheckBox(5, 36, 0xFFFFFFFF, I18n.format("gui.logistic_frame.fuzzyNBT")).withTag("fuzzyNBT");
        filterTab.addSubWidget(fuzzyNBT);
        if (logistics.supportsBlacklisting()) {
            whitelist = new WidgetCheckBox(5, 52, 0xFFFFFFFF, I18n.format("gui.logistic_frame.whitelist")).withTag("whitelist");
            filterTab.addSubWidget(whitelist);
        }
    }

    private void addFacingTab() {
        facingTab = addAnimatedStat("", new ItemStack(Items.MAP), 0xFFC0C0C0, false);
        facingTab.addPadding(8, 18);
        facingTab.addSubWidget(facingButtons[0] = new WidgetButtonExtended(15, 62, 20, 20,"D").withTag("side:0"));
        facingTab.addSubWidget(facingButtons[1] = new WidgetButtonExtended(15, 20, 20, 20,"U").withTag("side:1"));
        facingTab.addSubWidget(facingButtons[2] = new WidgetButtonExtended(36, 20, 20, 20,"N").withTag("side:2"));
        facingTab.addSubWidget(facingButtons[3] = new WidgetButtonExtended(36, 62, 20, 20,"S").withTag("side:3"));
        facingTab.addSubWidget(facingButtons[4] = new WidgetButtonExtended(15, 41, 20, 20,"W").withTag("side:4"));
        facingTab.addSubWidget(facingButtons[5] = new WidgetButtonExtended(57, 41, 20, 20,"E").withTag("side:5"));
        WidgetButtonExtended info = new WidgetButtonExtended(36, 41, 20, 20,"");
        info.setVisible(false);
        info.setTooltipText(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.logistic_frame.facing.tooltip")));
        info.setRenderedIcon(Textures.GUI_INFO_LOCATION);
        facingTab.addSubWidget(info);
    }

    @Override
    protected int getBackgroundTint() {
        if (!PNCConfig.Client.logisticsGuiTint) return super.getBackgroundTint();

        int c = logistics.getColor();
        // desaturate; this is a background colour...
        float[] hsb = Color.RGBtoHSB((c & 0xFF0000) >> 16, (c & 0xFF00) >> 8, c & 0xFF, null);
        Color color = Color.getHSBColor(hsb[0], hsb[1] * 0.2f, hsb[2]);
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
    public void tick() {
        super.tick();

        invisible.checked = logistics.isInvisible();
        fuzzyDamage.checked = logistics.isFuzzyDamage();
        fuzzyNBT.checked = logistics.isFuzzyNBT();
        if (logistics.supportsBlacklisting())
            whitelist.checked = logistics.isWhitelist();
        String s = logistics.getSide() == null ? "-" : logistics.getSide().getName();
        if (facingTab != null) {
            facingTab.setTitle(I18n.format("gui.logistic_frame.facing") + ": " + s);
            for (Direction face : Direction.values()) {
                facingButtons[face.getIndex()].active = face != logistics.getSide();
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int clickedButton, ClickType clickType) {
        if (slot instanceof SlotPhantom && minecraft.player.inventory.getItemStack().isEmpty() && !slot.getHasStack() && clickedButton == 1) {
            editingSlot = slot.getSlotIndex();
            ClientUtils.openContainerGui(ModContainerTypes.SEARCHER, new StringTextComponent("Searcher"));
            if (minecraft.currentScreen instanceof GuiItemSearcher) {
                searchGui = (GuiItemSearcher) minecraft.currentScreen;
            }
        } else {
            super.handleMouseClick(slot, slotId, clickedButton, clickType);
        }
    }
}
