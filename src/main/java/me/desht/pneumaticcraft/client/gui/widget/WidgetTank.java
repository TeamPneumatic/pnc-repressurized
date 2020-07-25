package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.thirdparty.ModNameCache;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

/**
 * This class is derived from BluePower and edited by MineMaarten:
 * https://github.com/Qmunity/BluePower/blob/FluidCrafting/src/main/java/com/bluepowermod/client/gui/widget/WidgetTank.java
 */
public class WidgetTank extends Widget implements ITooltipProvider {

    private final IFluidTank tank;

    public WidgetTank(int x, int y, IFluidTank tank) {
        super(x, y, 16, 64, StringTextComponent.EMPTY);
        this.tank = tank;
    }

    public WidgetTank(int x, int y, FluidStack stack) {
        this(x, y, makeTank(stack, 160000));
    }

    public WidgetTank(int x, int y, int width, int height, FluidStack stack) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.tank = makeTank(stack, stack.getAmount());
    }

    private static FluidTank makeTank(FluidStack stack, int capacity) {
        FluidTank tank = new FluidTank(capacity);
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return tank;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.disableLighting();
        GuiUtils.drawFluid(matrixStack, new Rectangle2d(x, y, width, height), getFluid(), getTank());

        // drawing a gauge rather than using the widget_tank texture since for some reason it doesn't work
        // https://github.com/desht/pnc-repressurized/issues/25
        matrixStack.push();
        matrixStack.translate(0, 0, 300);
        for (int i = 3; i < height - 1; i += 4) {
            int width = (i - 3) % 20 == 0 ? 16 : 2;
            AbstractGui.fill(matrixStack, x, y + i, x + width, y + i + 1, 0xFF2F2F2F);
        }
        matrixStack.pop();

//        GlStateManager.color(1, 1, 1, 1);
//        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.WIDGET_TANK);
//        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 64, 16, 64);
    }

    @Override
    public void addTooltip(double mouseX, double mouseY, List<ITextComponent> curTip, boolean shift) {
        Fluid fluid = tank.getFluid().getFluid();
        int amt = tank.getFluidAmount();
        int capacity = tank.getCapacity();

        curTip.add(new StringTextComponent(amt + "/" + capacity + " mB"));
        if (fluid == Fluids.EMPTY || amt == 0 || capacity == 0) {
            curTip.add(xlate("pneumaticcraft.gui.liquid.empty").mergeStyle(TextFormatting.GRAY));
        } else {
            curTip.add(new FluidStack(fluid, amt).getDisplayName().deepCopy().mergeStyle(TextFormatting.GRAY));
            curTip.add(new StringTextComponent(ModNameCache.getModName(fluid)).mergeStyle(TextFormatting.BLUE, TextFormatting.ITALIC));
        }
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    public FluidTank getTank() {
        return (FluidTank) tank;
    }
}
