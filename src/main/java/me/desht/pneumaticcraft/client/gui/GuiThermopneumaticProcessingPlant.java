package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<ContainerThermopneumaticProcessingPlant,TileEntityThermopneumaticProcessingPlant> {

    private WidgetTemperature tempWidget;
    private int nExposedFaces;

    public GuiThermopneumaticProcessingPlant(ContainerThermopneumaticProcessingPlant container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        ySize = 197;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 13, guiTop + 15, te.getInputTank()));
        addButton(new WidgetTank(guiLeft + 79, guiTop + 15, te.getOutputTank()));

        tempWidget = new WidgetTemperature(guiLeft + 98, guiTop + 15, 273, 673, te.getHeatExchangerLogic(null), (int) te.minTemperature) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<String> curTip, boolean shift) {
                super.addTooltip(mouseX, mouseY, curTip, shift);
                if (te.minTemperature > 0) {
                    TextFormatting tf = te.minTemperature < te.getHeatExchangerLogic(null).getTemperatureAsInt() ? TextFormatting.GREEN : TextFormatting.GOLD;
                    curTip.add(tf + "Required Temperature: " + (te.minTemperature - 273) + "\u00b0C");
                }
            }
        };
        addButton(tempWidget);

        WidgetButtonExtended dumpButton = new WidgetButtonExtended(guiLeft + 12, guiTop + 81, 18, 20, "").withTag("dump");
        dumpButton.setRenderedIcon(Textures.GUI_X_BUTTON);
        dumpButton.setTooltipText(PneumaticCraftUtils.convertStringIntoList(I18n.format("gui.thermopneumatic.dumpInput")));
        addButton(dumpButton);

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    @Override
    public void tick() {
        tempWidget.setScales((int) te.minTemperature);
        super.tick();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        bindGuiTexture();
        blit(guiLeft + 30, guiTop + 31, xSize, 0, progressWidth, 22);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        font.drawString(I18n.format("gui.tab.upgrades"), 91, 83, 4210752);
        String containerName = title.getFormattedText();
        GlStateManager.pushMatrix();
        GlStateManager.scaled(0.95, 0.97, 1);
        font.drawString(containerName, xSize / 2f - font.getStringWidth(containerName) / 2f + 1, 5, 4210752);
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
        } else if (te.getHeatExchangerLogic(null).getTemperatureAsInt() < te.minTemperature) {
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
