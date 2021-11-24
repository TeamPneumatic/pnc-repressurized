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

package me.desht.pneumaticcraft.client.model.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelDroneCore extends EntityModel<EntityDroneBase> {
    private final ModelRenderer Base2;
    private final ModelRenderer Base3;
    private final ModelRenderer Base4;
    private final ModelRenderer Base5;

    public ModelDroneCore() {
        texWidth = 64;
        texHeight = 32;

        Base2 = new ModelRenderer(64, 32, 0, 12);
        Base2.addBox(0F, 0F, 0F, 4, 4, 1);
        Base2.setPos(-2F, 15F, -4F);
        Base2.mirror = true;
        Base3 = new ModelRenderer(64, 32, 0, 12);
        Base3.addBox(0F, 0F, 0F, 4, 4, 1);
        Base3.setPos(-2F, 15F, 3F);
        Base3.mirror = true;
        Base4 = new ModelRenderer(64, 32, 10, 12);
        Base4.addBox(0F, 0F, 0F, 1, 4, 4);
        Base4.setPos(3F, 15F, -2F);
        Base4.mirror = true;
        Base5 = new ModelRenderer(64, 32, 10, 12);
        Base5.addBox(0F, 0F, 0F, 1, 4, 4);
        Base5.setPos(-4F, 15F, -2F);
        Base5.mirror = true;
    }

    @Override
    public void setupAnim(EntityDroneBase drone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Base2.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Base3.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Base4.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        Base5.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
