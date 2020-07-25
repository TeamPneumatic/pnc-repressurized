package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import static me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget.EnumInputOutput;

public class RenderTransferGadget extends RenderSemiblockBase<EntityTransferGadget> {
    public static final IRenderFactory<EntityTransferGadget> FACTORY = RenderTransferGadget::new;

    private static final float[] INPUT_COLS = new float[] { 0f, 0f, 1f, 0.75F };
    private static final float[] OUTPUT_COLS = new float[] { 1f, 0.3f, 0f, 0.75F };
    
    private RenderTransferGadget(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityTransferGadget entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entity.isAir() || entity.renderingOffset == null) {
            return;
        }
        
        matrixStackIn.push();

        // todo 1.15 transparent_model ?
        IVertexBuilder builder = bufferIn.getBuffer(ModRenderTypes.getUntexturedQuad(false));
        
        AxisAlignedBB aabb = entity.getBoundingBox();
        float[] cols = entity.getIOMode() == EnumInputOutput.OUTPUT ? OUTPUT_COLS : INPUT_COLS;
        Vector3d offset = entity.renderingOffset;
        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }
        matrixStackIn.translate(-entity.getPosX() + offset.getX(), -entity.getPosY() + offset.getY(), -entity.getPosZ() + offset.getZ());
        doRender(builder, aabb, cols, packedLightIn);
        
        matrixStackIn.pop();
    }

    private void doRender(IVertexBuilder wr, AxisAlignedBB aabb, float[] cols, int packedLightIn) {
        wr.pos(aabb.minX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();

        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();

        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();

        wr.pos(aabb.minX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.minY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();

        wr.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
        wr.pos(aabb.minX, aabb.maxY, aabb.minZ).color(cols[1], cols[2], cols[3], cols[0]).lightmap(packedLightIn).endVertex();
    }

    @Override
    public ResourceLocation getEntityTexture(EntityTransferGadget entityTransferGadget) {
        return null;
    }
}
