package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerFluidMixer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiFluidMixer extends GuiPneumaticContainerBase<ContainerFluidMixer, TileEntityFluidMixer> {
    private WidgetButtonExtended[] dumpButtons = new WidgetButtonExtended[2];

    public GuiFluidMixer(ContainerFluidMixer container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        ySize = 212;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 13, guiTop + 19, te.getInputTank1()));
        addButton(new WidgetTank(guiLeft + 33, guiTop + 19, te.getInputTank2()));
        addButton(new WidgetTank(guiLeft + 99, guiTop + 19, te.getOutputTank()));

        for (int i = 0; i < 2; i++) {
            dumpButtons[i] = new WidgetButtonExtended(guiLeft + 14 + i * 20, guiTop + 86, 14, 14, "")
                    .withTag("dump" + (i + 1));
            dumpButtons[i].setTooltipText(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.thermopneumatic.moveInput")));
            addButton(dumpButtons[i]);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_FLUID_MIXER;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, x, y);

        // animated progress bar
        int progressWidth = (int) (te.getCraftingPercentage() * 48);
        bindGuiTexture();
        blit(matrixStack, guiLeft + 50, guiTop + 36, xSize, 0, progressWidth, 30);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4 + 14, yStart + ySize / 4 - 2);
    }

    @Override
    public void tick() {
        super.tick();

        for (int i = 0; i < 2; i++) {
            String k = hasShiftDown() ? "dumpInput" : "moveInput";
            dumpButtons[i].setMessage(hasShiftDown() ? new StringTextComponent("X").mergeStyle(TextFormatting.RED) : new StringTextComponent(GuiConstants.TRIANGLE_RIGHT).mergeStyle(TextFormatting.DARK_AQUA));
//            dumpButtons[i].setRenderedIcon(hasShiftDown() ? Textures.GUI_X_BUTTON : Textures.GUI_RIGHT_ARROW);
            dumpButtons[i].setTooltipText(PneumaticCraftUtils.splitStringComponent(I18n.format("pneumaticcraft.gui.thermopneumatic." + k)));
        }
    }

    @Override
    protected void addPressureStatInfo(List<String> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.didWork) {
            pressureStatText.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tooltip.airUsage", PneumaticCraftUtils.roundNumberTo(2.5f * te.getPressure(), 2)));
        }
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);

        if (te.maxProgress == 0) {
            curInfo.add("pneumaticcraft.gui.tab.problems.missingIngredients");
        }
    }
}
