package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<TileEntityThermopneumaticProcessingPlant> {

    private WidgetTemperature tempWidget;

    public GuiThermopneumaticProcessingPlant(InventoryPlayer player, TileEntityThermopneumaticProcessingPlant te) {
        super(new ContainerThermopneumaticProcessingPlant(player, te), te, Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT);
        ySize = 197;
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 13, guiTop + 15, te.getInputTank()));
        addWidget(new WidgetTank(-1, guiLeft + 79, guiTop + 15, te.getOutputTank()));
        addWidget(tempWidget = new WidgetTemperature(-1, guiLeft + 98, guiTop + 15, 273, 673, te.getHeatExchangerLogic(null), (int) te.requiredTemperature));
    }

    @Override
    public void updateScreen() {
        tempWidget.setScales((int) te.requiredTemperature);
        super.updateScreen();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        //48 22
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        bindGuiTexture();
        drawTexturedModalRect(guiLeft + 30, guiTop + 31, xSize, 0, progressWidth, 22);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRenderer.drawString(I18n.format("gui.tab.upgrades"), 91, 83, 4210752);
        String containerName = I18n.format(te.getName() + ".name");
        GL11.glPushMatrix();
        GL11.glScaled(0.95, 0.97, 1);
        fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2 + 1, 5, 4210752);
        GL11.glPopMatrix();
        super.drawGuiContainerForegroundLayer(x, y);

    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

    @Override
    protected Point getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new Point(xStart + xSize * 3 / 4 + 10, yStart + ySize / 4);
    }

    @Override
    public void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (!te.hasRecipe) {
            curInfo.add("gui.tab.problems.thermopneumaticProcessingPlant.noSufficientIngredients");
        } else if (te.getHeatExchangerLogic(null).getTemperature() < te.requiredTemperature) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
    }

}
