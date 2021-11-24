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

package me.desht.pneumaticcraft.client.render.tube_module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModuleRedstone;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderRedstoneModule extends TubeModuleRendererBase<ModuleRedstone> {
    private final ModelRenderer redstoneConnector;
    private final ModelRenderer faceplate;
    private final ModelRenderer tubeConnector;
    private final ModelRenderer frame1;
    private final ModelRenderer frame2;
    private final ModelRenderer frame3;
    private final ModelRenderer frame4;

    public RenderRedstoneModule() {
        this.frame1 = new ModelRenderer(64, 32, 39, 0);
        this.frame1.setPos(-4F, 11.5F, 6.0F);
        this.frame1.addBox(0.0F, 0.0F, 0.0F, 8, 1, 1, 0.0F);
        this.frame2 = new ModelRenderer(64, 32, 42, 2);
        this.frame2.setPos(-4F, 19.5F, 6.0F);
        this.frame2.addBox(0.0F, 0.0F, 0.0F, 8, 1, 1, 0.0F);
        this.frame3 = new ModelRenderer(64, 32, 59, 3);
        this.frame3.setPos(3.5F, 12.5F, 6.0F);
        this.frame3.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);
        this.frame4 = new ModelRenderer(64, 32, 42, 4);
        this.frame4.setPos(-4.5F, 12.5F, 6.0F);
        this.frame4.addBox(0.0F, 0.0F, 0.0F, 1, 7, 1, 0.0F);

        this.tubeConnector = new ModelRenderer(64, 32, 30, 0);
        this.tubeConnector.setPos(-1.5F, 14.5F, 2.0F);
        this.tubeConnector.addBox(0.0F, 0.0F, 0.0F, 3, 3, 3, 0.0F);
        this.faceplate = new ModelRenderer(64, 32, 12, 0);
        this.faceplate.setPos(-4.0F, 12.0F, 5.0F);
        this.faceplate.addBox(0.0F, 0.0F, 0.0F, 8, 8, 1, 0.0F);
        this.redstoneConnector = new ModelRenderer(64, 32, 0, 0);
        this.redstoneConnector.setPos(-1.5F, 14.5F, 6.05F);
        this.redstoneConnector.addBox(0.0F, 0.0F, 0.0F, 3, 3, 3, 0.0F);
    }

    @Override
    protected void renderDynamic(ModuleRedstone module, MatrixStack matrixStack, IVertexBuilder builder, float partialTicks, int combinedLight, int combinedOverlay, float r, float g, float b, float a) {
        tubeConnector.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);
        faceplate.render(matrixStack, builder, combinedLight, combinedOverlay, r, g, b, a);

        float[] cols = { 1f, 1f, 1f, 1f };
        if (!module.isFake()) {
            int l = module.getRedstoneDirection() == ModuleRedstone.EnumRedstoneDirection.INPUT ? module.getInputLevel() : module.getRedstoneLevel();
            cols = RenderUtils.decomposeColorF(0xFF300000 | (l * 13 << 16));
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 5.2 / 16);
            matrixStack.scale(1, 1, 0.25f + 0.72f * MathHelper.lerp(partialTicks, module.lastExtension, module.extension));
            matrixStack.translate(0, 0, -5.2 / 16);
        }
        redstoneConnector.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        if (!module.isFake()) {
            matrixStack.popPose();
        }
        cols = RenderUtils.decomposeColorF(0xFF000000 | DyeColor.byId(module.getColorChannel()).getColorValue());
        frame1.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame2.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame3.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
        frame4.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_REDSTONE_MODULE;
    }
}
