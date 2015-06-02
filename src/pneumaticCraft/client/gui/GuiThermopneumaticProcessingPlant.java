package pneumaticCraft.client.gui;

import java.awt.Point;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.gui.widget.WidgetTank;
import pneumaticCraft.client.gui.widget.WidgetTemperature;
import pneumaticCraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import pneumaticCraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import pneumaticCraft.lib.Textures;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<TileEntityThermopneumaticProcessingPlant>{

    private WidgetTemperature tempWidget;

    public GuiThermopneumaticProcessingPlant(InventoryPlayer player, TileEntityThermopneumaticProcessingPlant te){
        super(new ContainerThermopneumaticProcessingPlant(player, te), te, Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT);
        ySize = 197;
    }

    @Override
    public void initGui(){
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 13, guiTop + 15, te.getInputTank()));
        addWidget(new WidgetTank(-1, guiLeft + 79, guiTop + 15, te.getOutputTank()));
        addWidget(tempWidget = new WidgetTemperature(-1, guiLeft + 98, guiTop + 15, 273, 673, te.getHeatExchangerLogic(ForgeDirection.UNKNOWN), (int)te.requiredTemperature));
    }

    @Override
    public void updateScreen(){
        tempWidget.setScales((int)te.requiredTemperature);
        super.updateScreen();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y){
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        //48 22
        double progress = te.getCraftingPercentage();
        int progressWidth = (int)(progress * 48);
        bindGuiTexture();
        drawTexturedModalRect(guiLeft + 30, guiTop + 31, xSize, 0, progressWidth, 22);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        fontRendererObj.drawString(I18n.format("gui.tab.upgrades"), 91, 83, 4210752);
        String containerName = te.hasCustomInventoryName() ? te.getInventoryName() : StatCollector.translateToLocal(te.getInventoryName() + ".name");
        GL11.glPushMatrix();
        GL11.glScaled(0.97, 0.97, 1);
        fontRendererObj.drawString(containerName, xSize / 2 - fontRendererObj.getStringWidth(containerName) / 2 + 1, 5, 4210752);
        GL11.glPopMatrix();
        super.drawGuiContainerForegroundLayer(x, y);

    }

    @Override
    protected Point getInvNameOffset(){
        return null;
    }

    @Override
    protected Point getGaugeLocation(){
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 10, yStart + ySize * 1 / 4);
    }

    @Override
    public void addProblems(List<String> curInfo){
        super.addProblems(curInfo);

        if(!te.hasRecipe) {
            curInfo.add("gui.tab.problems.thermopneumaticProcessingPlant.noSufficientIngredients");
        } else if(te.getHeatExchangerLogic(ForgeDirection.UNKNOWN).getTemperature() < te.requiredTemperature) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
    }

}
