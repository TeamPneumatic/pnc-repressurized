package pneumaticCraft.client.semiblock;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;

public class SemiBlockRendererLogistics implements ISemiBlockRenderer<SemiBlockLogistics>{

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick){

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glColor4d(1, 0, 0, 1);
        RenderUtils.glColorHex(semiBlock.getColor());
        double fw = 1 / 32D;
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(0 + fw, -fw, -fw, 1 - fw, fw, fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(0 + fw, 1 - fw, -fw, 1 - fw, 1 + fw, fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(0 + fw, -fw, 1 - fw, 1 - fw, fw, 1 + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(0 + fw, 1 - fw, 1 - fw, 1 - fw, 1 + fw, 1 + fw), 0, 0, 0);

        renderOffsetAABB(AxisAlignedBB.getBoundingBox(-fw, -fw, 0 + fw, fw, fw, 1 - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(-fw, 1 - fw, 0 + fw, fw, 1 + fw, 1 - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(1 - fw, -fw, 0 + fw, 1 + fw, fw, 1 - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(1 - fw, 1 - fw, 0 + fw, 1 + fw, 1 + fw, 1 - fw), 0, 0, 0);

        renderOffsetAABB(AxisAlignedBB.getBoundingBox(-fw, 0 - fw, -fw, fw, 1 + fw, fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(1 - fw, 0 - fw, -fw, 1 + fw, 1 + fw, fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(-fw, 0 - fw, 1 - fw, fw, 1 + fw, 1 + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(1 - fw, 0 - fw, 1 - fw, 1 + fw, 1 + fw, 1 + fw), 0, 0, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4d(1, 1, 1, 1);
    }

    public static void renderOffsetAABB(AxisAlignedBB p_76978_0_, double p_76978_1_, double p_76978_3_, double p_76978_5_){
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setTranslation(p_76978_1_, p_76978_3_, p_76978_5_);
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.minX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.minZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.maxY, p_76978_0_.maxZ);
        tessellator.addVertex(p_76978_0_.maxX, p_76978_0_.minY, p_76978_0_.maxZ);
        tessellator.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
    }
}
