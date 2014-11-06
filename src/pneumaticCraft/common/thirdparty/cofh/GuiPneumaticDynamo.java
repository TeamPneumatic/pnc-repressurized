package pneumaticCraft.common.thirdparty.cofh;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.client.gui.GuiPneumaticContainerBase;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.lib.Textures;

public class GuiPneumaticDynamo extends GuiPneumaticContainerBase<TileEntityPneumaticDynamo>{
    private final ContainerRF container;
    private GuiAnimatedStat inputStat;

    public GuiPneumaticDynamo(InventoryPlayer inventoryPlayer, TileEntityPneumaticDynamo te){
        super(new ContainerRF(inventoryPlayer, te), te, Textures.GUI_4UPGRADE_SLOTS);
        container = (ContainerRF)inventorySlots;
    }

    @Override
    public void initGui(){
        super.initGui();
        inputStat = addAnimatedStat("Input", (ItemStack)null, 0xFF555555, false);

        addWidget(new WidgetEnergy(guiLeft + 20, guiTop + 20, container));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        inputStat.setText(getOutputStat());
    }

    private List<String> getOutputStat(){
        List<String> textList = new ArrayList<String>();
        textList.add(EnumChatFormatting.GRAY + "Maximum RF production:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getRFRate() + " RF/tick");
        textList.add(EnumChatFormatting.GRAY + "Maximum output rate:");
        textList.add(EnumChatFormatting.BLACK.toString() + te.getRFRate() * 2 + " RF/tick");
        textList.add(EnumChatFormatting.GRAY + "Current stored RF:");
        textList.add(EnumChatFormatting.BLACK.toString() + container.energy + " RF");
        return textList;
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText){
        super.addPressureStatInfo(pressureStatText);
        pressureStatText.add("\u00a77Max Usage:");
        pressureStatText.add("\u00a70" + te.getAirRate() + " mL/tick.");
    }

}
