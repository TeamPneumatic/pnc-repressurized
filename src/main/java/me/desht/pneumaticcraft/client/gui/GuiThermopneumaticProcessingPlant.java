package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<TileEntityThermopneumaticProcessingPlant> {

    private WidgetTemperature tempWidget;
    private int nExposedFaces;

    public GuiThermopneumaticProcessingPlant(InventoryPlayer player, TileEntityThermopneumaticProcessingPlant te) {
        super(new ContainerThermopneumaticProcessingPlant(player, te), te, Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT);
        ySize = 197;
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(-1, guiLeft + 13, guiTop + 15, te.getInputTank()));
        addWidget(new WidgetTank(-1, guiLeft + 79, guiTop + 15, te.getOutputTank()));
        tempWidget = new WidgetTemperature(-1, guiLeft + 98, guiTop + 15, 273, (int)te.requiredTemperature, te.getHeatExchangerLogic(null), (int) te.requiredTemperature) {
            @Override
            public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);
                if (te.requiredTemperature > 0) {
                    TextFormatting tf = te.requiredTemperature < te.getHeatExchangerLogic(null).getTemperatureAsInt() ? TextFormatting.GREEN : TextFormatting.GOLD;
                    curTip.add(tf + "Required Temperature: " + (te.requiredTemperature - 273) + "\u00b0C");
                }
            }
        };

        addWidget(tempWidget);

        GuiButtonSpecial dumpButton = new GuiButtonSpecial(1, guiLeft + 12, guiTop + 81, 18, 20, "");
        dumpButton.setRenderedIcon(Textures.GUI_X_BUTTON);
        dumpButton.setTooltipText(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.thermopneumatic.dumpInput")));
        addWidget(dumpButton);

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    @Override
    public void updateScreen() {
        tempWidget.setScales((int) te.requiredTemperature);
        super.updateScreen();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        bindGuiTexture();
        drawTexturedModalRect(guiLeft + 30, guiTop + 31, xSize, 0, progressWidth, 22);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRenderer.drawString(I18n.format("gui.tab.upgrades"), 91, 83, 4210752);
        String containerName = I18n.format(te.getName() + ".name");
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.95, 0.97, 1);
        fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2 + 1, 5, 4210752);
        GlStateManager.popMatrix();
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
        } else if (te.getHeatExchangerLogic(null).getTemperatureAsInt() < te.requiredTemperature) {
            curInfo.add("gui.tab.problems.notEnoughHeat");
        }
    }

    @Override
    protected void addWarnings(List<String> curInfo) {
        super.addWarnings(curInfo);

        if (nExposedFaces > 0) {
            curInfo.add(I18n.format("gui.tab.problems.exposedFaces", nExposedFaces, 6));
        }
    }
}
