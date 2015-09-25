package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.client.gui.widget.WidgetLabel;
import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTextFieldNumber;
import pneumaticCraft.common.inventory.ContainerKeroseneLamp;
import pneumaticCraft.common.tileentity.TileEntityKeroseneLamp;
import pneumaticCraft.lib.Textures;

public class GuiKeroseneLamp extends GuiPneumaticContainerBase<TileEntityKeroseneLamp>{
    private WidgetLabel rangeTextWidget;
    //   private WidgetLabel timeLeftWidget;
    private WidgetTextFieldNumber rangeWidget;

    public GuiKeroseneLamp(InventoryPlayer player, TileEntityKeroseneLamp te){
        super(new ContainerKeroseneLamp(player, te), te, Textures.GUI_KEROSENE_LAMP);
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 152, guiTop + 15, te.getTank()));
        addWidget(rangeTextWidget = new WidgetLabel(guiLeft + 5, guiTop + 38, ""));
        //  addWidget(timeLeftWidget = new WidgetLabel(guiLeft + 5, guiTop + 26, ""));
        String maxRange = I18n.format("gui.keroseneLamp.maxRange");
        int maxRangeLength = fontRendererObj.getStringWidth(maxRange);
        addLabel(maxRange, guiLeft + 5, guiTop + 50);
        addLabel(I18n.format("gui.keroseneLamp.blocks"), guiLeft + maxRangeLength + 40, guiTop + 50);
        addWidget(rangeWidget = new WidgetTextFieldNumber(fontRendererObj, guiLeft + 7 + maxRangeLength, guiTop + 50, 30, fontRendererObj.FONT_HEIGHT));
        rangeWidget.minValue = 1;
        rangeWidget.maxValue = 30;
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        if(!rangeWidget.isFocused()) rangeWidget.setValue(te.getTargetRange());
        rangeTextWidget.text = I18n.format("gui.keroseneLamp.currentRange", te.getRange());

        /* if(te.getRange() > 0) {
             int ticksLeft = (int)(te.getTank().getFluidAmount() * TileEntityKeroseneLamp.FUEL_PER_MB * 5 / Math.pow(te.getRange(), 3));
             timeLeftWidget.text = I18n.format("gui.keroseneLamp.timeLeft", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(ticksLeft, false));
         } else {
             timeLeftWidget.text = "";
         }*/
    }

    @Override
    public String getRedstoneButtonText(int mode){
        if(mode == 3) return "gui.tab.redstoneBehaviour.keroseneLamp.button.interpolate";
        return super.getRedstoneButtonText(mode);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        if(!rangeWidget.isFocused()) {
            sendPacketToServer(rangeWidget.getValue());
        }
    }

    @Override
    protected void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getTank().getFluidAmount() == 0) {
            curInfo.add("gui.tab.problems.keroseneLamp.noFuel");
        } else if(te.getTank().getFluidAmount() < 30) {
            curInfo.add("gui.tab.problems.keroseneLamp.lowFuel");
        }
    }

}
