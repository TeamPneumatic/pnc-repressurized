package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelLogisticsFrame;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderLogisticsFrame extends RenderSemiblockBase<EntityLogisticsFrame> {
    public static final IRenderFactory<EntityLogisticsFrame> FACTORY = RenderLogisticsFrame::new;

    private final ModelLogisticsFrame model = new ModelLogisticsFrame();

    private RenderLogisticsFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityLogisticsFrame entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        float alpha = entity.getAlpha() / 255F;
        if (alpha == 0f) return;

        if (entity.isAir()) {
            return;
        }

        matrixStackIn.pushPose();

        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }

        Direction side = entity.getSide();
        matrixStackIn.translate(0, side.getAxis() == Direction.Axis.Y ? 0.5 : -0.5, 0);
        switch (side) {
            case UP:
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case DOWN:
                matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-90));
                matrixStackIn.translate(0, -1, 0);
                break;
            case NORTH:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90));
                break;
            case SOUTH:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-90));
                break;
            case WEST:
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180));
                break;
            case EAST:
                break;
        }

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        model.renderToBuffer(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.pack(0F, false), 1f, 1f, 1f, alpha);

        matrixStackIn.popPose();
    }

    @Override
    public Vector3d getRenderOffset(EntityLogisticsFrame entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.max(Direction.Axis.Y) - shape.min(Direction.Axis.Y)) / 2.0;
        switch (entityIn.getSide()) {
            case DOWN: return new Vector3d(0, shape.min(Direction.Axis.Y), 0);
            case UP: return new Vector3d(0, shape.max(Direction.Axis.Y) - 1, 0);
            case NORTH: return new Vector3d(0, yOff - 0.5, shape.min(Direction.Axis.Z));
            case SOUTH: return new Vector3d(0, yOff - 0.5, shape.max(Direction.Axis.Z) - 1);
            case WEST: return new Vector3d(shape.min(Direction.Axis.X), yOff - 0.5, 0);
            case EAST: return new Vector3d(shape.max(Direction.Axis.X) - 1, yOff - 0.5, 0);
            default: return Vector3d.ZERO;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLogisticsFrame entityLogisticsFrame) {
        return entityLogisticsFrame.getTexture();
    }
}
