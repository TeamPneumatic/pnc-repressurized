package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.block.tubes.ModuleSafetyValve;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderSafetyValveModule extends TubeModuleRendererBase<ModuleSafetyValve> {
    private final ModelRenderer tubeConnector;
    private final ModelRenderer valve;
    private final ModelRenderer valveHandle;
    private final ModelRenderer valveLid;

    public RenderSafetyValveModule(){
        tubeConnector = new ModelRenderer(32, 32, 0, 0);
        tubeConnector.setPos(-1.5F, 14.5F, 2.0F);
        tubeConnector.addBox(-0.5F, -0.5F, 0.0F, 4.0F, 4.0F, 2.0F);
        tubeConnector.mirror = true;

        valve = new ModelRenderer(32, 32, 0, 6);
        valve.setPos(-1.0F, 15.0F, 4.0F);
        valve.addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 4.0F);
        valve.mirror = true;

        valveHandle = new ModelRenderer(32, 32, 0, 16);
        valveHandle.setPos(2.0F, 15.5F, 4.0F);
        setRotation(valveHandle, 0.0F, -0.5934F, 0.0F);
        valveHandle.addBox(0.5592F, 0.0F, 0.829F, 1.0F, 1.0F, 3.0F);
        valveHandle.mirror = true;

        valveLid = new ModelRenderer(32, 32, 0, 12);
        valveLid.setPos(1.5F, 15.5F, 7.25F);
        valveLid.texOffs(0, 12).addBox(-3.0F, -1.0F, 0.0F, 3.0F, 3.0F, 1.0F);
    }

    @Override
    protected void renderDynamic(ModuleSafetyValve module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valve.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valveHandle.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        valveLid.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
    }

    @Override
    protected ResourceLocation getTexture() {
        ResourceLocation texture;
        if (isUpgraded()) {
            texture = Textures.MODEL_SAFETY_VALVE_UPGRADED;
        } else {
            texture = Textures.MODEL_SAFETY_VALVE;
        }
        return texture;
    }
}
