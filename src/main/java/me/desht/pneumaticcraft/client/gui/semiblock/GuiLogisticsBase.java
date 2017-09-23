package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.gui.GuiSearcher;
import me.desht.pneumaticcraft.client.gui.widget.GuiCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.IGuiWidget;
import me.desht.pneumaticcraft.client.gui.widget.WidgetFluidStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.inventory.SlotPhantom;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsFilterStack;
import me.desht.pneumaticcraft.common.network.PacketSetLogisticsFluidFilterStack;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

public class GuiLogisticsBase<Logistics extends SemiBlockLogistics> extends GuiPneumaticContainerBase {
    protected final Logistics logistics;
    private GuiSearcher searchGui;
    private GuiLogisticsLiquidFilter fluidSearchGui;
    private int editingSlot; //either fluid or item search.
    private GuiCheckBox invisible;

    public GuiLogisticsBase(InventoryPlayer invPlayer, Logistics requester) {
        super(new ContainerLogistics(invPlayer, requester), null, Textures.GUI_LOGISTICS_REQUESTER);
        logistics = (Logistics) ((ContainerLogistics) inventorySlots).logistics;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        if (searchGui != null) {
            inventorySlots.getSlot(editingSlot).putStack(searchGui.getSearchStack());
            NetworkHandler.sendToServer(new PacketSetLogisticsFilterStack(logistics, searchGui.getSearchStack(), editingSlot));
            searchGui = null;
        }
        if (fluidSearchGui != null && fluidSearchGui.getFilter() != null) {
            FluidStack filter = new FluidStack(fluidSearchGui.getFilter(), 1000);
            logistics.setFilter(editingSlot, filter);
            NetworkHandler.sendToServer(new PacketSetLogisticsFluidFilterStack(logistics, filter, editingSlot));
            fluidSearchGui = null;
        }
        String invisibleText = I18n.format("gui.logisticFrame.invisible");
        addWidget(invisible = new GuiCheckBox(9, guiLeft + xSize - 15 - fontRenderer.getStringWidth(invisibleText), guiTop + 7, 0xFF000000, invisibleText));
        invisible.setTooltip(Arrays.asList(WordUtils.wrap(I18n.format("gui.logisticFrame.invisible.tooltip"), 40).split(System.getProperty("line.separator"))));
        addWidget(new WidgetLabel(guiLeft + 8, guiTop + 18, I18n.format(String.format("gui.%s.filters", SemiBlockManager.getKeyForSemiBlock(logistics)))));
        addWidget(new WidgetLabel(guiLeft + 8, guiTop + 90, I18n.format("gui.logisticFrame.liquid")));
        for (int i = 0; i < 9; i++) {
            addWidget(new WidgetFluidStack(i, guiLeft + i * 18 + 8, guiTop + 101, logistics.getTankFilter(i)));
        }
        addInfoTab(I18n.format("gui.tab.info." + SemiBlockManager.getKeyForSemiBlock(logistics)));
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        invisible.checked = logistics.isInvisible();
    }

    @Override
    public void actionPerformed(IGuiWidget widget) {
        super.actionPerformed(widget);
        if (widget instanceof WidgetFluidStack) {
            boolean leftClick = Mouse.isButtonDown(0);
            boolean middleClick = Mouse.isButtonDown(2);
            boolean shift = PneumaticCraftRepressurized.proxy.isSneakingInGui();
            IFluidTank tank = logistics.getTankFilter(widget.getID());
            if (tank.getFluidAmount() > 0) {
                if (middleClick) {
                    logistics.setFilter(widget.getID(), null);
                } else if (leftClick) {
                    tank.drain(shift ? tank.getFluidAmount() / 2 : 1000, true);
                    if (tank.getFluidAmount() < 1000) {
                        tank.drain(1000, true);
                    }
                } else {
                    tank.fill(new FluidStack(tank.getFluid().getFluid(), shift ? tank.getFluidAmount() : 1000), true);
                }
                NetworkHandler.sendToServer(new PacketSetLogisticsFluidFilterStack(logistics, tank.getFluid(), widget.getID()));
            } else {
                fluidSearchGui = new GuiLogisticsLiquidFilter(this);
                editingSlot = widget.getID();
                mc.displayGuiScreen(fluidSearchGui);
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int clickedButton, ClickType clickType) {
        if (slot instanceof SlotPhantom && Minecraft.getMinecraft().player.inventory.getItemStack().isEmpty() && !slot.getHasStack() && clickedButton == 1) {
            editingSlot = slot.getSlotIndex();
            Minecraft.getMinecraft().displayGuiScreen(searchGui = new GuiSearcher(Minecraft.getMinecraft().player));
        } else {
            super.handleMouseClick(slot, slotId, clickedButton, clickType);
        }
    }
}
