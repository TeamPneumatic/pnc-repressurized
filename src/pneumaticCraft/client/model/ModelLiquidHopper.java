package pneumaticCraft.client.model;

import net.minecraftforge.fluids.FluidTankInfo;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.client.util.RenderUtils.RenderInfo;
import pneumaticCraft.common.tileentity.TileEntityLiquidHopper;
import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class ModelLiquidHopper extends ModelOmnidirectionalHopper{

    public ModelLiquidHopper(){
        super(Textures.MODEL_LIQUID_HOPPER);
    }

    @Override
    protected void renderMain(TileEntityOmnidirectionalHopper hopper){
        if(hopper != null) {
            TileEntityLiquidHopper liquidHopper = (TileEntityLiquidHopper)hopper;

            FluidTankInfo info = liquidHopper.getTankInfo(null)[0];
            int fluidAmount = info.fluid != null ? info.fluid.amount - info.capacity / 10 : 0;
            if(fluidAmount > 10) {
                GL11.glDisable(GL11.GL_LIGHTING);
                float percentageFull = Math.min(1, fluidAmount / (info.capacity * 0.3F));

                RenderInfo renderInfo = new RenderInfo(-4 / 16F + 0.001F, 12 / 16F + 0.001F, 4 / 16F - percentageFull * 5.999F / 16F, 4 / 16F - 0.001F, 20 / 16F - 0.001F, 4 / 16F - 0.001F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());

                fluidAmount -= info.capacity * 0.3F;
                if(fluidAmount > 10) {
                    percentageFull = Math.min(1, fluidAmount / (info.capacity * 0.3F));

                    renderInfo = new RenderInfo(-6 / 16F + 0.001F, 10 / 16F + 0.001F, -2 / 16F - percentageFull * 0.999F / 16F, 6 / 16F - 0.001F, 22 / 16F - 0.001F, -2 / 16F - 0.001F);
                    RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());

                    fluidAmount -= info.capacity * 0.3F;
                    if(fluidAmount > 10) {
                        percentageFull = Math.min(1, fluidAmount / (info.capacity * 0.3F));
                        renderInfo = new RenderInfo(6 / 16F + 0.001F, 8 / 16F + 0.001F, -2 / 16F - percentageFull * 5.999F / 16F, 8 / 16F - 0.001F, 24 / 16F - 0.001F, -2 / 16F - 0.001F);
                        RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());

                        renderInfo = new RenderInfo(-8 / 16F + 0.001F, 8 / 16F + 0.001F, -2 / 16F - percentageFull * 5.999F / 16F, -6 / 16F - 0.001F, 24 / 16F - 0.001F, -2 / 16F - 0.001F);
                        RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());

                        renderInfo = new RenderInfo(-6 / 16F + 0.001F, 22 / 16F + 0.001F, -2 / 16F - percentageFull * 5.999F / 16F, 6 / 16F - 0.001F, 24 / 16F - 0.001F, -2 / 16F - 0.001F);
                        RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());

                        renderInfo = new RenderInfo(-6 / 16F + 0.001F, 8 / 16F + 0.001F, -2 / 16F - percentageFull * 5.999F / 16F, 6 / 16F - 0.001F, 10 / 16F - 0.001F, -2 / 16F - 0.001F);
                        RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());
                    }
                }

                FMLClientHandler.instance().getClient().getTextureManager().bindTexture(getModelTexture(hopper));
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glColor4d(1, 1, 1, 1);
            }
        }
    }

    @Override
    protected void renderBottom(TileEntityOmnidirectionalHopper hopper){
        if(hopper != null) {

            TileEntityLiquidHopper liquidHopper = (TileEntityLiquidHopper)hopper;
            FluidTankInfo info = liquidHopper.getTankInfo(null)[0];
            if(info.fluid != null && info.fluid.amount > 10) {
                GL11.glDisable(GL11.GL_LIGHTING);
                float percentageFull = Math.min(1, (float)info.fluid.amount / (info.capacity / 10));
                RenderInfo renderInfo = new RenderInfo(-2 / 16F + 0.001F, 14 / 16F + 0.001F, 8 / 16F - percentageFull * 3.999F / 16F, 2 / 16F - 0.001F, 18 / 16F - 0.001F, 8 / 16F - 0.001F);
                RenderUtils.INSTANCE.renderLiquid(info, renderInfo, liquidHopper.getWorldObj());
                GL11.glEnable(GL11.GL_LIGHTING);
            }
        }
    }

}
