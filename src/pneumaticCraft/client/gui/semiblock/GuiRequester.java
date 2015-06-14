package pneumaticCraft.client.gui.semiblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.GuiSearcher;
import pneumaticCraft.client.gui.widget.IGuiWidget;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.common.inventory.ContainerRequester;
import pneumaticCraft.common.inventory.SlotPhantom;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSetRequesterStack;
import pneumaticCraft.common.semiblock.SemiBlockRequester;
import pneumaticCraft.lib.Textures;

public class GuiRequester extends GuiPneumaticContainerBase{
    private final SemiBlockRequester requester;
    private GuiSearcher searchGui;
    private int editingSlot;

    public GuiRequester(InventoryPlayer invPlayer, SemiBlockRequester requester){
        super(new ContainerRequester(invPlayer, requester), null, Textures.GUI_LOGISTICS_REQUESTER);
        this.requester = ((ContainerRequester)inventorySlots).requester;
        ySize = 205;
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetLabel(guiLeft + 8, guiTop + 7, I18n.format("gui.logisticsRequester.requests")));
        addInfoTab(I18n.format("gui.tab.info.logisticsFrameRequester"));
        if(searchGui != null) {
            inventorySlots.getSlot(editingSlot).putStack(searchGui.getSearchStack());
            NetworkHandler.sendToServer(new PacketSetRequesterStack(requester, searchGui.getSearchStack(), editingSlot));
        }
    }

    @Override
    public void actionPerformed(GuiButton button){}

    @Override
    public void actionPerformed(IGuiWidget widget){}

    @Override
    protected boolean shouldAddProblemTab(){
        return false;
    }

    @Override
    protected void handleMouseClick(Slot slot, int x, int mouse, int y){
        if(slot instanceof SlotPhantom && Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null && !slot.getHasStack() && mouse == 1) {
            editingSlot = slot.getSlotIndex();
            Minecraft.getMinecraft().displayGuiScreen(searchGui = new GuiSearcher(Minecraft.getMinecraft().thePlayer));
        } else {
            super.handleMouseClick(slot, x, y, mouse);
        }
    }
}
