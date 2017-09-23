package me.desht.pneumaticcraft.client.gui.widget;

import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is derived from BluePower and edited by MineMaarten:
 * https://github.com/Qmunity/BluePower/blob/FluidCrafting/src/main/java/com/bluepowermod/client/gui/widget/WidgetTank.java
 */
public class WidgetTank extends WidgetBase {

    private final IFluidTank tank;
    private static final int TEX_WIDTH = 16;
    private static final int TEX_HEIGHT = 16;
    private static final int MIN_FLUID_HEIGHT = 0;

    public WidgetTank(int id, int x, int y, IFluidTank tank) {
        super(id, x, y, 16, 64);
        this.tank = tank;
    }

    public WidgetTank(int x, int y, FluidStack stack) {
        super(-1, x, y, 16, 64);
        tank = new FluidTank(stack, 16000);
    }

    public WidgetTank(int x, int y, int width, int height, FluidStack stack) {
        super(-1, x, y, width, height);
        tank = new FluidTank(stack, stack.amount);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        GL11.glDisable(GL11.GL_LIGHTING);
        drawFluid(Minecraft.getMinecraft(), x, y, getFluid());

        GL11.glColor4d(1, 1, 1, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(Textures.WIDGET_TANK);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 64, 16, 64);
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shift) {
        Fluid fluid = null;
        int amt = 0;
        int capacity = 0;

        if (tank.getFluid() != null) {
            fluid = tank.getFluid().getFluid();
            amt = tank.getFluidAmount();
        }
        capacity = tank.getCapacity();

        if (fluid == null || amt == 0 || capacity == 0) {
            curTip.add(amt + "/" + capacity + " mb");
            curTip.add(TextFormatting.GRAY + I18n.format("gui.liquid.empty"));
        } else {
            curTip.add(amt + "/" + capacity + " mb");
            curTip.add(TextFormatting.GRAY + fluid.getLocalizedName(new FluidStack(fluid, amt)));
        }
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getTank() {
        return (FluidTank) tank;
    }

    /**
     * Method derived from JEI, which is released under the MIT licence:
     * https://github.com/mezz/JustEnoughItems
     * The class mezz.jei.gui.ingredients.FluidStackRenderer
     *
     * @param minecraft
     * @param xPosition
     * @param yPosition
     * @param fluidStack
     */
    private void drawFluid(@Nonnull Minecraft minecraft, final int xPosition, final int yPosition, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            return;
        }
        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return;
        }

        TextureMap textureMapBlocks = minecraft.getTextureMapBlocks();
        ResourceLocation fluidStill = fluid.getStill();
        TextureAtlasSprite fluidStillSprite = null;
        if (fluidStill != null) {
            fluidStillSprite = textureMapBlocks.getTextureExtry(fluidStill.toString());
        }
        if (fluidStillSprite == null) {
            fluidStillSprite = textureMapBlocks.getMissingSprite();
        }

        int fluidColor = fluid.getColor(fluidStack);

        int scaledAmount = fluidStack.amount * getBounds().height / getTank().getCapacity();
        if (fluidStack.amount > 0 && scaledAmount < MIN_FLUID_HEIGHT) {
            scaledAmount = MIN_FLUID_HEIGHT;
        }
        if (scaledAmount > getBounds().height) {
            scaledAmount = getBounds().height;
        }

        minecraft.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderUtils.glColorHex(fluidColor, 255);

        final int xTileCount = getBounds().width / TEX_WIDTH;
        final int xRemainder = getBounds().width - xTileCount * TEX_WIDTH;
        final int yTileCount = scaledAmount / TEX_HEIGHT;
        final int yRemainder = scaledAmount - yTileCount * TEX_HEIGHT;

        final int yStart = yPosition + getBounds().height;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                int w = xTile == xTileCount ? xRemainder : TEX_WIDTH;
                int h = yTile == yTileCount ? yRemainder : TEX_HEIGHT;
                int x = xPosition + xTile * TEX_WIDTH;
                int y = yStart - (yTile + 1) * TEX_HEIGHT;
                if (getBounds().width > 0 && h > 0) {
                    int maskTop = TEX_HEIGHT - h;
                    int maskRight = TEX_WIDTH - w;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 100);
                }
            }
        }
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, int maskTop, int maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        worldrenderer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        worldrenderer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        worldrenderer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }
}
