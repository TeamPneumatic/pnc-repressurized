package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.inventory.ContainerThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant;
import me.desht.pneumaticcraft.common.tileentity.TileEntityThermopneumaticProcessingPlant.TPProblem;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GuiThermopneumaticProcessingPlant extends
        GuiPneumaticContainerBase<ContainerThermopneumaticProcessingPlant,TileEntityThermopneumaticProcessingPlant> {

    private WidgetTemperature tempWidget;
    private WidgetButtonExtended dumpButton;
    private int nExposedFaces;

    public GuiThermopneumaticProcessingPlant(ContainerThermopneumaticProcessingPlant container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        ySize = 212;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_THERMOPNEUMATIC_PROCESSING_PLANT;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 13, guiTop + 19, te.getInputTank()));
        addButton(new WidgetTank(guiLeft + 79, guiTop + 19, te.getOutputTank()));

        tempWidget = new WidgetTemperature(guiLeft + 105, guiTop + 25, TemperatureRange.of(273, 673), 273, 50);
        addButton(tempWidget);

        dumpButton = new WidgetButtonExtended(guiLeft + 14, guiTop + 86, 14, 14, StringTextComponent.EMPTY)
                .withTag("dump");
        addButton(dumpButton);

        nExposedFaces = HeatUtil.countExposedFaces(Collections.singletonList(te));
    }

    @Override
    public void tick() {
        super.tick();

        if (te.maxTemperature > te.minTemperature && !te.getCurrentRecipeIdSynced().isEmpty()) {
            tempWidget.setOperatingRange(TemperatureRange.of(te.minTemperature, te.maxTemperature));
        } else {
            tempWidget.setOperatingRange(null);
        }
        tempWidget.setTemperature(te.getHeatExchanger().getTemperatureAsInt());
        tempWidget.autoScaleForTemperature();

        if (hasShiftDown()) {
            dumpButton.setMessage(new StringTextComponent("X").mergeStyle(TextFormatting.RED));
            dumpButton.setTooltipKey("pneumaticcraft.gui.thermopneumatic.dumpInput");
        } else {
            dumpButton.setMessage(new StringTextComponent(GuiConstants.TRIANGLE_RIGHT).mergeStyle(TextFormatting.DARK_AQUA));
            dumpButton.setTooltipKey("pneumaticcraft.gui.thermopneumatic.moveInput");
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, x, y);

        // animated progress bar
        double progress = te.getCraftingPercentage();
        int progressWidth = (int) (progress * 48);
        bindGuiTexture();
        blit(matrixStack, guiLeft + 30, guiTop + 36, xSize, 0, progressWidth, 30);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        matrixStack.push();
        matrixStack.scale(0.95f, 1f, 1f);
        font.func_238422_b_(matrixStack, title.func_241878_f(), xSize / 2f - font.getStringPropertyWidth(title) / 2.1f , 5, 0x404040);
        matrixStack.pop();
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);

    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4 + 14, yStart + ySize / 4 - 2);
    }

    @Override
    public void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.problem != null && te.problem != TPProblem.OK) {
            curInfo.addAll(GuiUtils.xlateAndSplit(te.problem.getTranslationKey()));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);

        if (nExposedFaces > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.exposedFaces", nExposedFaces, 6));
        }
    }

    @Override
    public Collection<ItemStack> getTargetItems() {
        return getCurrentRecipe(PneumaticCraftRecipeType.THERMO_PLANT)
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.getOutputItem()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(PneumaticCraftRecipeType.THERMO_PLANT)
                .map(thermoPlantRecipe -> Collections.singletonList(thermoPlantRecipe.getOutputFluid()))
                .orElse(Collections.emptyList());
    }
}
