package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyIOUnit;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;

public class RenderAssemblyIOUnit extends AbstractTileModelRenderer<TileEntityAssemblyIOUnit> {
    private final ModelRenderer baseTurn;
    private final ModelRenderer baseTurn2;
    private final ModelRenderer armBase1;
    private final ModelRenderer armBase2;
    private final ModelRenderer supportMiddle;
    private final ModelRenderer armMiddle1;
    private final ModelRenderer armMiddle2;
    private final ModelRenderer clawBase;
    private final ModelRenderer clawAxle;
    private final ModelRenderer clawTurn;
    private final ModelRenderer claw1;
    private final ModelRenderer claw2;

    private static final float ITEM_SCALE = 0.5F;

    public RenderAssemblyIOUnit(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        baseTurn = new ModelRenderer(64, 64, 0, 17);
        baseTurn.addBox(0F, 0F, 0F, 7, 1, 7);
        baseTurn.setRotationPoint(-3.5F, 22F, -3.5F);
        baseTurn.mirror = true;
        baseTurn2 = new ModelRenderer(64, 64, 28, 17);
        baseTurn2.addBox(0F, 0F, 0F, 4, 5, 4);
        baseTurn2.setRotationPoint(-2F, 17F, -2F);
        baseTurn2.mirror = true;
        armBase1 = new ModelRenderer(64, 64, 0, 25);
        armBase1.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase1.setRotationPoint(2F, 17F, -1F);
        armBase1.mirror = true;
        armBase2 = new ModelRenderer(64, 64, 0, 25);
        armBase2.addBox(0F, 0F, 0F, 1, 2, 8);
        armBase2.setRotationPoint(-3F, 17F, -1F);
        armBase2.mirror = true;
        supportMiddle = new ModelRenderer(64, 64, 0, 57);
        supportMiddle.addBox(0F, 0F, 0F, 2, 1, 1);
        supportMiddle.setRotationPoint(-1F, 17.5F, 5.5F);
        supportMiddle.mirror = true;
        armMiddle1 = new ModelRenderer(64, 64, 0, 35);
        armMiddle1.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle1.setRotationPoint(-2F, 2F, 5F);
        armMiddle1.mirror = true;
        armMiddle2 = new ModelRenderer(64, 64, 0, 35);
        armMiddle2.addBox(0F, 0F, 0F, 1, 17, 2);
        armMiddle2.setRotationPoint(1F, 2F, 5F);
        armMiddle2.mirror = true;
        clawBase = new ModelRenderer(64, 64, 8, 38);
        clawBase.addBox(0F, 0F, 0F, 2, 2, 3);
        clawBase.setRotationPoint(-1F, 2F, 4.5F);
        clawBase.mirror = true;
        clawAxle = new ModelRenderer(64, 64, 8, 45);
        clawAxle.addBox(0F, 0F, 0F, 1, 1, 1);
        clawAxle.setRotationPoint(-0.5F, 2.5F, 4F);
        clawAxle.mirror = true;
        clawTurn = new ModelRenderer(64, 64, 8, 49);
        clawTurn.addBox(0F, 0F, 0F, 4, 2, 1);
        clawTurn.setRotationPoint(-2F, 2F, 3F);
        clawTurn.mirror = true;
        claw1 = new ModelRenderer(64, 64, 8, 54);
        claw1.addBox(0F, 0F, 0F, 1, 2, 1);
        claw1.setRotationPoint(0F, 2F, 2F);
        claw1.mirror = true;
        claw2 = new ModelRenderer(64, 64, 8, 59);
        claw2.addBox(0F, 0F, 0F, 1, 2, 1);
        claw2.setRotationPoint(-1F, 2F, 2F);
        claw2.mirror = true;
    }

    @Override
    void renderModel(TileEntityAssemblyIOUnit te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        float[] angles = new float[5];
        for (int i = 0; i < 5; i++) {
            angles[i] = te.oldAngles[i] + (te.angles[i] - te.oldAngles[i]) * partialTicks;
        }

        ItemStack heldStack = te.getPrimaryInventory().getStackInSlot(0);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(getTexture(te)));
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(MathHelper.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angles[0]));
        baseTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        baseTurn2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 0);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[1]));
        matrixStackIn.translate(0, -18 / 16F, 0);
        armBase1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armBase2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        supportMiddle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 18 / 16F, 6 / 16F);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[2]));
        matrixStackIn.translate(0, -18 / 16F, -6 / 16F);
        armMiddle1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        armMiddle2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 3 / 16F, 6 / 16F);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(angles[3]));
        matrixStackIn.translate(0, -3 / 16F, -6 / 16F);
        clawBase.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 3 / 16F, 0);
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(angles[4]));
        matrixStackIn.translate(0, -3 / 16F, 0);
        clawAxle.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        clawTurn.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.push();
        matrixStackIn.translate(clawTranslation.getRight(), 0, 0);
        claw1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(-2 * clawTranslation.getRight(), 0, 0);
        claw2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.pop();

        if (!heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = clawTranslation.getLeft();
            if (renderOverride == null || renderOverride.applyRenderChangeIOUnit(matrixStackIn, heldStack)) {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                double yOffset = heldStack.getItem() instanceof BlockItem ? 1.5 / 16D : 0.5 / 16D;
                matrixStackIn.translate(0, yOffset, -3 / 16D);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90));
                matrixStackIn.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
                IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(heldStack, te.getWorld(), null);
                itemRenderer.renderItem(heldStack, ItemCameraTransforms.TransformType.FIXED, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, ibakedmodel);
            }
        }
    }

    private Pair<IAssemblyRenderOverriding, Float> getClawTranslation(float clawProgress, ItemStack heldStack) {
        float clawTrans;
        IAssemblyRenderOverriding renderOverride = null;
        if (!heldStack.isEmpty()) {
            renderOverride = GuiRegistry.renderOverrides.get(heldStack.getItem().getRegistryName());
            if (renderOverride != null) {
                clawTrans = renderOverride.getIOUnitClawShift(heldStack);
            } else {
                if (heldStack.getItem() instanceof BlockItem) {
                    clawTrans = 1.5F / 16F - clawProgress * 0.1F / 16F;
                } else {
                    clawTrans = 1.5F / 16F - clawProgress * 1.4F / 16F;
                }
            }
        } else {
            clawTrans = 1.5F / 16F - clawProgress * 1.5F / 16F;
        }
        return Pair.of(renderOverride, clawTrans);
    }

    private ResourceLocation getTexture(TileEntityAssemblyIOUnit te) {
        return te != null && te.isImportUnit() ? Textures.MODEL_ASSEMBLY_IO_IMPORT : Textures.MODEL_ASSEMBLY_IO_EXPORT;
    }
}
