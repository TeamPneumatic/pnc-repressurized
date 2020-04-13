package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.MathHelper;

public class RenderPneumaticDoor extends AbstractTileModelRenderer<TileEntityPneumaticDoor> {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;
    private final ModelRenderer shape5;
    private final ModelRenderer shape6;
    private final ModelRenderer shape7;
    private final ModelRenderer shape8;
    private final ModelRenderer shape9;

    public RenderPneumaticDoor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        
        shape1 = new ModelRenderer(64, 32, 0, 24);
        shape1.addBox(0F, 0F, 0F, 16, 3, 3);
        shape1.setRotationPoint(-8F, -8F, -8F);
        shape1.mirror = true;
        shape2 = new ModelRenderer(64, 32, 38, 0);
        shape2.addBox(0F, 0F, 0F, 3, 3, 3);
        shape2.setRotationPoint(-8F, -5F, -8F);
        shape2.mirror = true;
        shape3 = new ModelRenderer(64, 32, 50, 0);
        shape3.addBox(0F, 0F, 0F, 2, 3, 3);
        shape3.setRotationPoint(-1F, -5F, -8F);
        shape3.mirror = true;
        shape4 = new ModelRenderer(64, 32, 38, 6);
        shape4.addBox(0F, 0F, 0F, 3, 3, 3);
        shape4.setRotationPoint(5F, -5F, -8F);
        shape4.mirror = true;
        shape5 = new ModelRenderer(64, 32, 0, 24);
        shape5.addBox(0F, 0F, 0F, 16, 2, 3);
        shape5.setRotationPoint(-8F, -2F, -8F);
        shape5.mirror = true;
        shape6 = new ModelRenderer(64, 32, 38, 12);
        shape6.addBox(0F, 0F, 0F, 3, 3, 3);
        shape6.setRotationPoint(-8F, 0F, -8F);
        shape6.mirror = true;
        shape7 = new ModelRenderer(64, 32, 50, 12);
        shape7.addBox(0F, 0F, 0F, 2, 3, 3);
        shape7.setRotationPoint(-1F, 0F, -8F);
        shape7.mirror = true;
        shape8 = new ModelRenderer(64, 32, 38, 18);
        shape8.addBox(0F, 0F, 0F, 3, 3, 3);
        shape8.setRotationPoint(5F, 0F, -8F);
        shape8.mirror = true;
        shape9 = new ModelRenderer(64, 32, 0, 0);
        shape9.addBox(0F, 0F, 0F, 16, 21, 3);
        shape9.setRotationPoint(-8F, 3F, -8F);
        shape9.mirror = true;
    }

    @Override
    public void renderModel(TileEntityPneumaticDoor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.getBlockState().get(BlockPneumaticDoor.TOP_DOOR)) return;

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_PNEUMATIC_DOOR_DYNAMIC));

        float rotation = MathHelper.lerp(partialTicks, te.oldRotationAngle, te.rotationAngle);
        boolean rightGoing = te.rightGoing;
        float[] rgb = DyeColor.byId(te.color).getColorComponentValues();

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        matrixStackIn.translate((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        matrixStackIn.rotate(rightGoing ? Vector3f.YN.rotationDegrees(rotation) : Vector3f.YP.rotationDegrees(rotation));
        matrixStackIn.translate((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);

        shape1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape3.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape4.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape5.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape6.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape7.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape8.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
        shape9.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
    }
}
