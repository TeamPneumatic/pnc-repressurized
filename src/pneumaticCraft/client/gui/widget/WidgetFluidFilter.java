package pneumaticCraft.client.gui.widget;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class WidgetFluidFilter extends WidgetBase{
    protected Fluid fluid;

    public WidgetFluidFilter(int id, int x, int y){
        super(id, x, y, 16, 16);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick){
        if(fluid != null) {
            IIcon icon = fluid.getIcon();
            if(icon != null) {
                GL11.glColor4d(1, 1, 1, 1);
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                t.addVertexWithUV(0, 0, 0, icon.getMinU(), icon.getMinV());
                t.addVertexWithUV(0, 16, 0, icon.getMinU(), icon.getMaxV());
                t.addVertexWithUV(16, 16, 0, icon.getMaxU(), icon.getMaxV());
                t.addVertexWithUV(16, 0, 0, icon.getMaxU(), icon.getMinV());
                t.draw();
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTip, boolean shiftPressed){
        if(fluid != null) curTip.add(fluid.getLocalizedName(new FluidStack(fluid, 1)));
    }

    public WidgetFluidFilter setFluid(Fluid fluid){
        this.fluid = fluid;
        return this;
    }

    public Fluid getFluid(){
        return fluid;
    }
}
