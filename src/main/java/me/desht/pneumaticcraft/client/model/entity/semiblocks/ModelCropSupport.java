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
import me.desht.pneumaticcraft.common.entity.semiblock.EntityCropSupport;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelCropSupport extends EntityModel<EntityCropSupport> {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModelRenderer shape3;
    private final ModelRenderer shape4;

    public ModelCropSupport() {
        texWidth = 64;
        texHeight = 64;

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
    }

    @Override
    public void setupAnim(EntityCropSupport entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        shape1.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        shape4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
