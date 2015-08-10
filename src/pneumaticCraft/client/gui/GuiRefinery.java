package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.inventory.ContainerRefinery;
import pneumaticCraft.common.tileentity.TileEntityRefinery;
import pneumaticCraft.lib.Textures;

public class GuiRefinery extends GuiPneumaticContainerBase<TileEntityRefinery>{
    private List<TileEntityRefinery> refineries;

    public GuiRefinery(InventoryPlayer player, TileEntityRefinery te){
        super(new ContainerRefinery(player, te), te, Textures.GUI_REFINERY);
    }

    @Override
    public void initGui(){
        super.initGui();

        addWidget(new WidgetTemperature(-1, guiLeft + 32, guiTop + 20, 273, 673, te.getHeatExchangerLogic(ForgeDirection.UNKNOWN), 373));

        addWidget(new WidgetTank(-1, guiLeft + 8, guiTop + 13, te.getOilTank()));

        int x = guiLeft + 95;
        int y = guiTop + 17;
        addWidget(new WidgetTank(-1, x, y, te.getOutputTank()));

        refineries = new ArrayList<TileEntityRefinery>();
        refineries.add(te);
        TileEntityRefinery refinery = te;
        while(refinery.getTileCache()[ForgeDirection.UP.ordinal()].getTileEntity() instanceof TileEntityRefinery) {
            refinery = (TileEntityRefinery)refinery.getTileCache()[ForgeDirection.UP.ordinal()].getTileEntity();
            x += 20;
            y -= 4;
            if(refineries.size() < 4) addWidget(new WidgetTank(-1, x, y, refinery.getOutputTank()));
            refineries.add(refinery);
        }

        if(refineries.size() < 2 || refineries.size() > 4) {
            problemTab.openWindow();
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        fontRendererObj.drawString(I18n.format(Blockss.refinery.getUnlocalizedName() + ".name"), 28, 5, 4210752);
        super.drawGuiContainerForegroundLayer(x, y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y){
        super.drawGuiContainerBackgroundLayer(f, x, y);
        if(refineries.size() < 4) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            bindGuiTexture();
            drawTexturedModalRect(guiLeft + 155, guiTop + 5, xSize, 0, 16, 64);
            //drawTexturedModalRect(guiLeft + 48, guiTop + 22, xSize + 16, 0, 26, 10);
            if(refineries.size() < 3) {
                drawTexturedModalRect(guiLeft + 135, guiTop + 9, xSize, 0, 16, 64);
                // drawTexturedModalRect(guiLeft + 48, guiTop + 32, xSize + 42, 0, 26, 15);
            }
        }
    }

    @Override
    protected Point getInvTextOffset(){
        return new Point(20, -1);
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);
        if(te.getHeatExchangerLogic(ForgeDirection.UNKNOWN).getTemperature() < 395) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
        if(te.getOilTank().getFluidAmount() < 10) {
            curInfo.add("gui.tab.problems.refinery.noOil");
        }
        if(refineries.size() < 2) {
            curInfo.add("gui.tab.problems.refinery.notEnoughRefineries");
        } else if(refineries.size() > 4) {
            curInfo.add("gui.tab.problems.refinery.tooManyRefineries");
        } else if(!te.refine(refineries, true)) {
            curInfo.add("gui.tab.problems.refinery.outputBlocked");
        }
    }
}
