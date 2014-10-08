package pneumaticCraft.client.util;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import buildcraft.core.render.RenderEntityBlock.RenderInfo;

public class RenderUtils extends Render{
    public static RenderUtils INSTANCE = new RenderUtils();
    public RenderBlocks renderBlocks;

    private RenderUtils(){
        renderBlocks = field_147909_c;
    }

    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, int x, int y, int z, boolean doLight, boolean doTessellating){
        renderBlock(info, blockAccess, x, y, z, x, y, z, doLight, doTessellating);
    }

    public void renderBlock(RenderInfo info, IBlockAccess blockAccess, double x, double y, double z, int lightX, int lightY, int lightZ, boolean doLight, boolean doTessellating){
        float lightBottom = 0.5F;
        float lightTop = 1.0F;
        float lightEastWest = 0.8F;
        float lightNorthSouth = 0.6F;

        Tessellator tessellator = Tessellator.instance;

        boolean realDoLight = doLight;

        if(blockAccess == null) {
            realDoLight = false;
        }

        // TODO: needs to cancel the test because the variable is now private... May need to
        // duplicate the tessellator code.
        //if (doTessellating && !tessellator.isDrawing)
        tessellator.startDrawingQuads();

        float light = 0;
        if(realDoLight) {
            if(info.light < 0) {
                light = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
                light = light + (1.0f - light) * 0.4f;
            } else {
                light = info.light;
            }
            int brightness = 0;
            if(info.brightness < 0) {
                brightness = info.baseBlock.getMixedBrightnessForBlock(blockAccess, lightX, lightY, lightZ);
            } else {
                brightness = info.brightness;
            }
            tessellator.setBrightness(brightness);
            tessellator.setColorOpaque_F(lightBottom * light, lightBottom * light, lightBottom * light);
        } else {
            //          tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
            if(info.brightness >= 0) {
                tessellator.setBrightness(info.brightness);
            }
        }

        renderBlocks.setRenderBounds(info.minX, info.minY, info.minZ, info.maxX, info.maxY, info.maxZ);

        if(info.renderSide[0]) {
            renderBlocks.renderFaceYNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(0));
        }

        if(realDoLight) {
            tessellator.setColorOpaque_F(lightTop * light, lightTop * light, lightTop * light);
        }

        if(info.renderSide[1]) {
            renderBlocks.renderFaceYPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(1));
        }

        if(realDoLight) {
            tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
        }

        if(info.renderSide[2]) {
            renderBlocks.renderFaceZNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(2));
        }

        if(realDoLight) {
            tessellator.setColorOpaque_F(lightEastWest * light, lightEastWest * light, lightEastWest * light);
        }

        if(info.renderSide[3]) {
            renderBlocks.renderFaceZPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(3));
        }

        if(realDoLight) {
            tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
        }

        if(info.renderSide[4]) {
            renderBlocks.renderFaceXNeg(info.baseBlock, x, y, z, info.getBlockTextureFromSide(4));
        }

        if(realDoLight) {
            tessellator.setColorOpaque_F(lightNorthSouth * light, lightNorthSouth * light, lightNorthSouth * light);
        }

        if(info.renderSide[5]) {
            renderBlocks.renderFaceXPos(info.baseBlock, x, y, z, info.getBlockTextureFromSide(5));
        }

        // TODO: needs to cancel the test because the variable is now private... May need to
        // duplicate the tessellator code.
        //if (doTessellating && tessellator.isDrawing)
        tessellator.draw();
    }

    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_){

    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_){
        return null;
    }
}
