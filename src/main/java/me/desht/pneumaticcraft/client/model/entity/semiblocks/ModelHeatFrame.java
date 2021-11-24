/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.model.entity.semiblocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelHeatFrame extends EntityModel<EntityHeatFrame> {
    //fields
    private final ModelRenderer bottom1;
    private final ModelRenderer bottom2;
    private final ModelRenderer bottom3;
    private final ModelRenderer bottom4;
    private final ModelRenderer bottom5;
    private final ModelRenderer bottom6;
    private final ModelRenderer bottom7;
    private final ModelRenderer bottom8;
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;
    private final ModelRenderer top6;
    private final ModelRenderer top3;
    private final ModelRenderer top4;
    private final ModelRenderer top8;
    private final ModelRenderer top1;
    private final ModelRenderer top5;
    private final ModelRenderer top2;
    private final ModelRenderer top7;

    public ModelHeatFrame() {
        texWidth = 64;
        texHeight = 64;

        bottom3 = new ModelRenderer(64, 64, 0, 0);
        bottom3.addBox(0F, 0F, 0F, 4, 4, 4);
        bottom3.setPos(-8.5F, 20.5F, 4.5F);
        bottom3.mirror = true;
        bottom4 = new ModelRenderer(64, 64, 16, 0);
        bottom4.addBox(0F, 0F, 0F, 4, 4, 4);
        bottom4.setPos(4.5F, 20.5F, 4.5F);
        bottom4.mirror = true;
        bottom5 = new ModelRenderer(64, 64, 32, 0);
        bottom5.addBox(0F, 0F, 0F, 9, 1, 1);
        bottom5.setPos(-4.5F, 23.5F, -8.5F);
        bottom5.mirror = true;
        bottom6 = new ModelRenderer(64, 64, 32, 2);
        bottom6.addBox(0F, 0F, 0F, 9, 1, 1);
        bottom6.setPos(-4.5F, 23.5F, 7.5F);
        bottom6.mirror = true;
        bottom7 = new ModelRenderer(64, 64, 44, 0);
        bottom7.addBox(0F, 0F, 0F, 1, 1, 9);
        bottom7.setPos(-8.5F, 23.5F, -4.5F);
        bottom7.mirror = true;
        bottom8 = new ModelRenderer(64, 64, 32, 4);
        bottom8.addBox(0F, 0F, 0F, 1, 1, 9);
        bottom8.setPos(7.5F, 23.5F, -4.5F);
        bottom8.mirror = true;
        bottom2 = new ModelRenderer(64, 64, 0, 8);
        bottom2.addBox(0F, 0F, 0F, 4, 4, 4);
        bottom2.setPos(-8.5F, 20.5F, -8.5F);
        bottom2.mirror = true;
        bottom1 = new ModelRenderer(64, 64, 16, 8);
        bottom1.addBox(0F, 0F, 0F, 4, 4, 4);
        bottom1.setPos(4.5F, 20.5F, -8.5F);
        bottom1.mirror = true;
        shape1 = new ModelRenderer(64, 64, 0, 16);
        shape1.addBox(0F, 0F, 0F, 1, 9, 1);
        shape1.setPos(-8.5F, 11.5F, -8.5F);
        shape1.mirror = true;
        shape2 = new ModelRenderer(64, 64, 4, 16);
        shape2.addBox(0F, 0F, 0F, 1, 9, 1);
        shape2.setPos(7.5F, 11.5F, -8.5F);
        shape2.mirror = true;
        shape3 = new ModelRenderer(64, 64, 0, 16);
        shape3.addBox(0F, 0F, 0F, 1, 9, 1);
        shape3.setPos(-8.5F, 11.5F, 7.5F);
        shape3.mirror = true;
        shape4 = new ModelRenderer(64, 64, 0, 16);
        shape4.addBox(0F, 0F, 0F, 1, 9, 1);
        shape4.setPos(7.5F, 11.5F, 7.5F);
        shape4.mirror = true;
        top6 = new ModelRenderer(64, 64, 32, 26);
        top6.addBox(0F, 0F, 0F, 9, 1, 1);
        top6.setPos(-4.5F, 7.5F, -8.5F);
        top6.mirror = true;
        top3 = new ModelRenderer(64, 64, 0, 34);
        top3.addBox(0F, 0F, 0F, 4, 4, 4);
        top3.setPos(-8.5F, 7.5F, -8.5F);
        top3.mirror = true;
        top4 = new ModelRenderer(64, 64, 16, 26);
        top4.addBox(0F, 0F, 0F, 4, 4, 4);
        top4.setPos(4.5F, 7.5F, 4.5F);
        top4.mirror = true;
        top8 = new ModelRenderer(64, 64, 32, 30);
        top8.addBox(0F, 0F, 0F, 1, 1, 9);
        top8.setPos(-8.5F, 7.5F, -4.5F);
        top8.mirror = true;
        top1 = new ModelRenderer(64, 64, 0, 26);
        top1.addBox(0F, 0F, 0F, 4, 4, 4);
        top1.setPos(-8.5F, 7.5F, 4.5F);
        top1.mirror = true;
        top5 = new ModelRenderer(64, 64, 32, 28);
        top5.addBox(0F, 0F, 0F, 9, 1, 1);
        top5.setPos(-4.5F, 7.5F, 7.5F);
        top5.mirror = true;
        top2 = new ModelRenderer(64, 64, 16, 34);
        top2.addBox(0F, 0F, 0F, 4, 4, 4);
        top2.setPos(4.5F, 7.5F, -8.5F);
        top2.mirror = true;
        top7 = new ModelRenderer(64, 64, 44, 26);
        top7.addBox(0F, 0F, 0F, 1, 1, 9);
        top7.setPos(7.5F, 7.5F, -4.5F);
        top7.mirror = true;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        bottom3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom6.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom8.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        bottom1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top6.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top8.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        top7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(EntityHeatFrame entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
