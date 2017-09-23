package me.desht.pneumaticcraft.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class WidgetFluidFilter extends WidgetBase {
    protected Fluid fluid;

    public WidgetFluidFilter(int id, int x, int y) {
        super(id, x, y, 16, 16);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (fluid != null) {
            ResourceLocation icon = fluid.getStill(); //TODO 1.8 still or flowing?
            if (icon != null) {
                GL11.glColor4d(1, 1, 1, 1);
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0);
                Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
                BufferBuilder wr = Tessellator.getInstance().getBuffer();
                wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                wr.pos(0, 0, 0).tex(0, 0).endVertex();
                wr.pos(0, 16, 0).tex(0, 1).endVertex();
                wr.pos(16, 16, 0).tex(1, 1).endVertex();
                wr.pos(16, 0, 0).tex(1, 0).endVertex();
                Tessellator.getInstance().draw();
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed) {
        if (fluid != null) curTip.add(fluid.getLocalizedName(new FluidStack(fluid, 1)));
    }

    public WidgetFluidFilter setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    public Fluid getFluid() {
        return fluid;
    }
}
