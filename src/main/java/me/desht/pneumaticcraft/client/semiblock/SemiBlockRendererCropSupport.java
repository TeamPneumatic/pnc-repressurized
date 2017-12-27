package me.desht.pneumaticcraft.client.semiblock;

import me.desht.pneumaticcraft.client.model.semiblocks.ModelCropSupport;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockCropSupport;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class SemiBlockRendererCropSupport implements ISemiBlockRenderer<SemiBlockCropSupport> {
    private final ModelCropSupport model = new ModelCropSupport();

    @Override
    public void render(SemiBlockCropSupport semiBlock, float partialTick) {
        GL11.glPushMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Textures.MODEL_HEAT_FRAME);

        double brightness = 0.2;
        GL11.glColor4d(brightness, brightness, brightness, 1);

        AxisAlignedBB aabb = new AxisAlignedBB(3 / 16D, -6 / 16D, 3 / 16D, 13 / 16D, 17 / 16D, 13 / 16D);
       
        GL11.glTranslated(aabb.minX, aabb.minY, aabb.minZ);
        GL11.glScaled(aabb.maxX - aabb.minX, aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ);
        GL11.glTranslated(0.5, -0.5, 0.5);
        model.render(null, 0, 0, 0, 0, 0, 1 / 16F);
        GL11.glPopMatrix();
        GL11.glColor4d(1, 1, 1, 1);
    }
}
