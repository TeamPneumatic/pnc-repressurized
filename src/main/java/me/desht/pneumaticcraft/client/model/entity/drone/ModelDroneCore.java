package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDroneCore extends EntityModel<EntityDroneBase> {
    private final ModelRenderer done;
    private final ModelRenderer body;

    public ModelDroneCore() {
        texWidth = 128;
        texHeight = 128;

        done = new ModelRenderer(this);
        done.setPos(0.0F, 22.5F, 0.0F);

        body = new ModelRenderer(this);
        body.setPos(0.0F, -3.0F, 0.0F);
        done.addChild(body);
        body.texOffs(48, 106).addBox(-3.5F, -5.0F, -4.5F, 7.0F, 6.0F, 16.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(EntityDroneBase drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        done.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
