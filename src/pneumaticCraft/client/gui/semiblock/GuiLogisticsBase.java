package pneumaticCraft.client.gui.semiblock;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Mouse;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.GuiSearcher;
import pneumaticCraft.client.gui.widget.GuiCheckBox;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetFluidStack;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.common.inventory.ContainerLogistics;
import pneumaticCraft.common.inventory.SlotPhantom;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetLogisticsFilterStack;
import pneumaticCraft.common.network.PacketSetLogisticsFluidFilterStack;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.lib.Textures;

public class GuiLogisticsBase<Logistics extends SemiBlockLogistics> extends GuiPneumaticContainerBase{
    protected final Logistics logistics;
    private GuiSearcher searchGui;
    private GuiLogisticsLiquidFilter fluidSearchGui;
    private int editingSlot; //either fluid or item search.
    private GuiCheckBox invisible;

    public GuiLogisticsBase(InventoryPlayer invPlayer, Logistics requester){
        super(new ContainerLogistics(invPlayer, requester), null, Textures.GUI_LOGISTICS_REQUESTER);
        logistics = (Logistics)((ContainerLogistics)inventorySlots).logistics;
        ySize = 216;
    }

    @Override
    public void initGui(){
        super.initGui();

        if(searchGui != null) {
            inventorySlots.getSlot(editingSlot).putStack(searchGui.getSearchStack());
            NetworkHandler.sendToServer(new PacketSetLogisticsFilterStack(logistics, searchGui.getSearchStack(), editingSlot));
            searchGui = null;
        }
        if(fluidSearchGui != null && fluidSearchGui.getFilter() != null) {
            FluidStack filter = new FluidStack(fluidSearchGui.getFilter(), 1000);
            logistics.setFilter(editingSlot, filter);
            NetworkHandler.sendToServer(new PacketSetLogisticsFluidFilterStack(logistics, filter, editingSlot));
            fluidSearchGui = null;
        }
        String invisibleText = I18n.format("gui.logisticFrame.invisible");
        addWidget(invisible = new GuiCheckBox(9, guiLeft + xSize - 15 - fontRendererObj.getStringWidth(invisibleText), guiTop + 7, 0xFF000000, invisibleText));
        invisible.setTooltip(Arrays.asList(WordUtils.wrap(I18n.format("gui.logisticFrame.invisible.tooltip"), 40).split(System.getProperty("line.separator"))));
        addWidget(new WidgetLabel(guiLeft + 8, guiTop + 18, I18n.format(String.format("gui.%s.filters", SemiBlockManager.getKeyForSemiBlock(logistics)))));
        addWidget(new WidgetLabel(guiLeft + 8, guiTop + 90, I18n.format("gui.logisticFrame.liquid")));
        for(int i = 0; i < 9; i++) {
            addWidget(new WidgetFluidStack(i, guiLeft + i * 18 + 8, guiTop + 101, logistics.getTankFilter(i)));
        }
        addInfoTab(I18n.format("gui.tab.info." + SemiBlockManager.getKeyForSemiBlock(logistics)));
    }

    @Override
    protected boolean shouldAddProblemTab(){
        return false;
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        invisible.checked = logistics.isInvisible();
    }

    @Override
    public void actionPerformed(IGuiWidget widget){
        super.actionPerformed(widget);
        if(widget instanceof WidgetFluidStack) {
            boolean leftClick = Mouse.isButtonDown(0);
            boolean middleClick = Mouse.isButtonDown(2);
            boolean shift = PneumaticCraft.proxy.isSneakingInGui();
            IFluidTank tank = logistics.getTankFilter(widget.getID());
            if(tank.getFluidAmount() > 0) {
                if(middleClick) {
                    logistics.setFilter(widget.getID(), (FluidStack)null);
                } else if(leftClick) {
                    tank.drain(shift ? tank.getFluidAmount() / 2 : 1000, true);
                    if(tank.getFluidAmount() < 1000) {
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
    protected void handleMouseClick(Slot slot, int x, int mouse, int y){
        if(slot instanceof SlotPhantom && Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null && !slot.getHasStack() && mouse == 1) {
            editingSlot = slot.getSlotIndex();
            Minecraft.getMinecraft().displayGuiScreen(searchGui = new GuiSearcher(Minecraft.getMinecraft().thePlayer));
        } else {
            super.handleMouseClick(slot, x, mouse, y);
        }
    }
}
