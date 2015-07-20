package pneumaticCraft.client.semiblock;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.util.RenderUtils;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;

public class SemiBlockRendererLogistics implements ISemiBlockRenderer<SemiBlockLogistics>{

    @Override
    public void render(SemiBlockLogistics semiBlock, float partialTick){
        int alpha = semiBlock.getAlpha();
        if(alpha == 0) return;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glColor4d(1, 0, 0, 1);
        if(alpha < 255) GL11.glEnable(GL11.GL_BLEND);
        RenderUtils.glColorHex((alpha << 24 | 0x00FFFFFF) & semiBlock.getColor());
        double fw = 1 / 32D;
        AxisAlignedBB aabb;
        if(semiBlock.getWorld() != null) {
            semiBlock.getBlock().setBlockBoundsBasedOnState(semiBlock.getWorld(), semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosY, semiBlock.getPos().chunkPosZ);
            aabb = semiBlock.getBlock().getSelectedBoundingBoxFromPool(semiBlock.getWorld(), semiBlock.getPos().chunkPosX, semiBlock.getPos().chunkPosY, semiBlock.getPos().chunkPosZ);
        } else {
            aabb = AxisAlignedBB.getBoundingBox(0 + fw, 0 + fw, 0 + fw, 1 - fw, 1 - fw, 1 - fw);
        }

        if(semiBlock.getPos() != null) GL11.glTranslated(-semiBlock.getPos().chunkPosX, -semiBlock.getPos().chunkPosY, -semiBlock.getPos().chunkPosZ);

        renderFrame(aabb, fw);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4d(1, 1, 1, 1);
    }

    public static void renderFrame(AxisAlignedBB aabb, double fw){
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX + fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX + fw, aabb.maxY - fw, aabb.minZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX + fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.minY + fw, aabb.maxZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX + fw, aabb.maxY - fw, aabb.maxZ - fw, aabb.maxX - fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);

        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.minY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.maxX - fw, aabb.minY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.minY + fw, aabb.maxZ - fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.maxX - fw, aabb.maxY - fw, aabb.minZ + fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ - fw), 0, 0, 0);

        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.maxX - fw, aabb.minY - fw, aabb.minZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.minZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.minX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.minX + fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);
        renderOffsetAABB(AxisAlignedBB.getBoundingBox(aabb.maxX - fw, aabb.minY - fw, aabb.maxZ - fw, aabb.maxX + fw, aabb.maxY + fw, aabb.maxZ + fw), 0, 0, 0);
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
