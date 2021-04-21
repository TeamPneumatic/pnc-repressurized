package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.ModelTransferGadget;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderTransferGadget extends RenderSemiblockBase<EntityTransferGadget> {
    public static final IRenderFactory<EntityTransferGadget> FACTORY = RenderTransferGadget::new;

    private final ModelTransferGadget model = new ModelTransferGadget();

    private RenderTransferGadget(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityTransferGadget entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entity.isAir()) {
            return;
        }
        
        matrixStackIn.push();

        if (entity.getTimeSinceHit() > 0) {
            wobble(entity, partialTicks, matrixStackIn);
        }

        Direction side = entity.getSide();
        matrixStackIn.translate(0, side.getAxis() == Axis.Y ? 1.2 : -1.1, 0);
        switch (side) {
            case UP:
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
                matrixStackIn.translate(-1.1, -1.1, 0);
                break;
            case DOWN:
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-90));
                matrixStackIn.translate(1.3, -1.1, 0);
                break;
            case NORTH:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));
                break;
            case SOUTH:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
                break;
            case WEST:
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
                break;
        }

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntitySolid(getEntityTexture(entity)));
        model.render(matrixStackIn, builder, kludgeLightingLevel(entity, packedLightIn), OverlayTexture.getPackedUV(0F, false), 1f, 1f, 1f, 1f);

        matrixStackIn.pop();
    }

    @Override
    public Vector3d getRenderOffset(EntityTransferGadget entityIn, float partialTicks) {
        VoxelShape shape = entityIn.getBlockState().getShape(entityIn.getWorld(), entityIn.getBlockPos());
        double yOff = (shape.getEnd(Axis.Y) - shape.getStart(Axis.Y)) / 2.0;
        switch (entityIn.getSide()) {
            case DOWN: return new Vector3d(0, shape.getStart(Axis.Y), 0);
            case UP: return new Vector3d(0, shape.getEnd(Axis.Y), 0);
            case NORTH: return new Vector3d(0, yOff, shape.getStart(Axis.Z) - 0.6);
            case SOUTH: return new Vector3d(0, yOff, shape.getEnd(Axis.Z) - 0.4);
            case WEST: return new Vector3d(shape.getStart(Axis.X) - 0.6, yOff, 0);
            case EAST: return new Vector3d(shape.getEnd(Axis.X) - 0.4, yOff, 0);
            default: return Vector3d.ZERO;
        }
    }

    @Override
    public ResourceLocation getEntityTexture(EntityTransferGadget entityTransferGadget) {
        return entityTransferGadget.getIOMode().getTexture();
    }
}
