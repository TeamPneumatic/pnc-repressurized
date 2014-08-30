package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.ModelPressureTube;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.block.tubes.TubeModule;
import pneumaticCraft.common.tileentity.TileEntityPressureTube;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderPressureTube extends TileEntitySpecialRenderer{

    private final ModelPressureTube model;

    public RenderPressureTube(){
        model = new ModelPressureTube();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f){
        renderModelAt((TileEntityPressureTube)tileentity, d0, d1, d2, f);
    }

    public void renderModelAt(TileEntityPressureTube tile, double d, double d1, double d2, float f){
        GL11.glPushMatrix(); // start
        // GL11.glDisable(GL11.GL_TEXTURE_2D);
        // GL11.glEnable(GL11.GL_BLEND);
        // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(tile.getBlockType() == Blockss.pressureTube ? Textures.MODEL_PRESSURE_TUBE : Textures.MODEL_ADVANCED_PRESSURE_TUBE);

        // GL11.glColor4f(0.82F, 0.56F, 0.09F, 1.0F);
        GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F); // size
        GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);

        GL11.glScalef(1.0F, -1F, -1F); // to make your block have a normal
                                       // positioning. comment out to see what
                                       // happens
        model.renderModel(0.0625F, tile.sidesConnected);
        // GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix(); // end

        for(TubeModule module : tile.modules) {
            if(module != null) module.renderDynamic(d, d1, d2, f, 0);
        }
    }

}
