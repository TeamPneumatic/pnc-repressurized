package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.api.client.assembly_machine.IAssemblyRenderOverriding;
import me.desht.pneumaticcraft.client.GuiRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyPlatform;
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
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.Pair;

public class RenderAssemblyPlatform extends AbstractTileModelRenderer<TileEntityAssemblyPlatform> {
    private static final float ITEM_SCALE = 0.5F;

    private final ModelRenderer claw1;
    private final ModelRenderer claw2;

    public RenderAssemblyPlatform(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        claw1 = new ModelRenderer(64, 64, 0, 32);
        claw1.addBox(0F, 0F, 0F, 2, 1, 1);
        claw1.setRotationPoint(-1F, 17F, 0F);
        claw1.mirror = true;
        claw2 = new ModelRenderer(64, 64, 0, 32);
        claw2.addBox(0F, 0F, 0F, 2, 1, 1);
        claw2.setRotationPoint(-1F, 17F, -1F);
        claw2.mirror = true;
    }

    @Override
    public void renderModel(TileEntityAssemblyPlatform te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ItemStack heldStack = te.getPrimaryInventory().getStackInSlot(0);
        Pair<IAssemblyRenderOverriding, Float> clawTranslation = getClawTranslation(MathHelper.lerp(partialTicks, te.oldClawProgress, te.clawProgress), heldStack);
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(Textures.MODEL_ASSEMBLY_PLATFORM));

        matrixStackIn.push();
        matrixStackIn.translate(0, 0, clawTranslation.getRight());
        claw1.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.translate(0, 0, -2 * clawTranslation.getRight());
        claw2.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
        matrixStackIn.pop();

        if (!heldStack.isEmpty()) {
            IAssemblyRenderOverriding renderOverride = clawTranslation.getLeft();
            if (renderOverride == null || renderOverride.applyRenderChangePlatform(matrixStackIn, heldStack)) {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
                double yOffset = heldStack.getItem() instanceof BlockItem ? -16.5 / 16F : -17.5 / 16F;
                matrixStackIn.translate(0, yOffset + 0.05, 0);
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
                clawTrans = renderOverride.getPlatformClawShift(heldStack);
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
}
