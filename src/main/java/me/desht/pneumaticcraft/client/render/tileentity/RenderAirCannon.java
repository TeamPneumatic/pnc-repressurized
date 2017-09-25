package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAirCannon;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderAirCannon extends TileEntitySpecialRenderer<TileEntityAirCannon> {
    private final ModelAirCannon model;

    public RenderAirCannon() {
        model = new ModelAirCannon();
    }

    @Override
    public void render(TileEntityAirCannon te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GL11.glPushMatrix(); // start
        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_AIR_CANNON);
        GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); // size
        // to make your block have a normal positioning. comment out to see what happens
        GL11.glScalef(1.0F, -1F, -1F);
        float angle = (float) PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        float rotationAngle = te.rotationAngle - angle + 180F;
        model.renderModel(0.0625F, rotationAngle, te.heightAngle);
        GL11.glPopMatrix(); // end
    }
}
