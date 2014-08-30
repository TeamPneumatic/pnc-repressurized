package pneumaticCraft.client.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.ModelAirCannon;
import pneumaticCraft.common.tileentity.TileEntityAirCannon;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderAirCannon extends TileEntitySpecialRenderer{

    ModelAirCannon model;

    public RenderAirCannon(){
        model = new ModelAirCannon();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f){
        renderModelAt((TileEntityAirCannon)tileentity, d0, d1, d2, f);
    }

    // 2, 5, 3, 4

    public void renderModelAt(TileEntityAirCannon tile, double d, double d1, double d2, float f){
        GL11.glPushMatrix(); // start
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_AIR_CANNON);
        GL11.glTranslatef((float)d + 0.5F, (float)d1 + 1.5F, (float)d2 + 0.5F); // size
        GL11.glScalef(1.0F, -1F, -1F); // to make your block have a normal
                                       // positioning. comment out to see what
                                       // happens
        float angle = (float)PneumaticCraftUtils.rotateMatrixByMetadata(tile.getBlockMetadata());
        float rotationAngle = tile.rotationAngle - angle + 180F;
        model.renderModel(0.0625F, rotationAngle, tile.heightAngle, false, false);
        GL11.glPopMatrix(); // end

    }

}
