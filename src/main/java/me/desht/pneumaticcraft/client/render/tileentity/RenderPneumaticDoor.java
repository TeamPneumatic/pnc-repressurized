package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderPneumaticDoor extends TileEntitySpecialRenderer<TileEntityPneumaticDoor> {
    private final ModelDoor modelDoor;

    public RenderPneumaticDoor() {
        modelDoor = new ModelDoor();
    }

    @Override
    public void render(TileEntityPneumaticDoor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.rotationAngle == 0f || te.rotationAngle == 90f) return;

        GL11.glPushMatrix();

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_PNEUMATIC_DOOR_DYNAMIC);
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata() % 6);
        float rotation = te.oldRotationAngle + (te.rotationAngle - te.oldRotationAngle) * partialTicks;
        boolean rightGoing = te.rightGoing;
        GL11.glTranslatef((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        GL11.glRotatef(rotation, 0, rightGoing ? -1 : 1, 0);
        GL11.glTranslatef((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);
        if (te.getBlockMetadata() < 6) {
            modelDoor.renderModel(0.0625f);
        }

        GL11.glPopMatrix();
    }

}
