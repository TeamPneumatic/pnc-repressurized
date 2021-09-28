package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.inventory.ContainerFluidMixer;
import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import me.desht.pneumaticcraft.common.tileentity.TileEntityFluidMixer;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
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

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiFluidMixer extends GuiPneumaticContainerBase<ContainerFluidMixer, TileEntityFluidMixer> {
    private final WidgetButtonExtended[] dumpButtons = new WidgetButtonExtended[2];

    public GuiFluidMixer(ContainerFluidMixer container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
        imageHeight = 212;
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(leftPos + 13, topPos + 19, te.getInputTank1()));
        addButton(new WidgetTank(leftPos + 33, topPos + 19, te.getInputTank2()));
        addButton(new WidgetTank(leftPos + 99, topPos + 19, te.getOutputTank()));

        for (int i = 0; i < 2; i++) {
            dumpButtons[i] = new WidgetButtonExtended(leftPos + 14 + i * 20, topPos + 86, 14, 14, StringTextComponent.EMPTY)
                    .withTag("dump" + (i + 1));
            addButton(dumpButtons[i]);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_FLUID_MIXER;
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int x, int y) {
        super.renderBg(matrixStack, partialTicks, x, y);

        // animated progress bar
        int progressWidth = (int) (te.getCraftingPercentage() * 48);
        bindGuiTexture();
        blit(matrixStack, leftPos + 50, topPos + 36, imageWidth, 0, progressWidth, 30);
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 14, yStart + imageHeight / 4 - 2);
    }

    @Override
    public void tick() {
        super.tick();

        for (int i = 0; i < 2; i++) {
            String k = hasShiftDown() ? "dumpInput" : "moveInput";
            dumpButtons[i].setMessage(hasShiftDown() ? new StringTextComponent("X").withStyle(TextFormatting.RED) : new StringTextComponent(Symbols.TRIANGLE_RIGHT).withStyle(TextFormatting.DARK_AQUA));
            dumpButtons[i].setTooltipKey("pneumaticcraft.gui.thermopneumatic." + k);
        }
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);

        if (te.didWork) {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticCraftUtils.roundNumberTo(2.5f * te.getPressure(), 2)));
        }
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);

        if (te.maxProgress == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.missingIngredients"));
        }
    }

    @Override
    public Collection<ItemStack> getTargetItems() {
        return getCurrentRecipe(PneumaticCraftRecipeType.FLUID_MIXER)
                .map(recipe -> Collections.singletonList(recipe.getOutputItem()))
                .orElse(Collections.emptyList());
    }

    @Override
    public Collection<FluidStack> getTargetFluids() {
        return getCurrentRecipe(PneumaticCraftRecipeType.FLUID_MIXER)
                .map(recipe -> Collections.singletonList(recipe.getOutputFluid()))
                .orElse(Collections.emptyList());
    }
}
