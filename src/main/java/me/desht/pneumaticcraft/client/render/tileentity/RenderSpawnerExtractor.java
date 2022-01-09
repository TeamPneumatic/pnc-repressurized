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

package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.fluid.AbstractFluidTER;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.FluidStack;

public class RenderSpawnerExtractor extends AbstractTileModelRenderer<TileEntitySpawnerExtractor> {
    private static final AxisAlignedBB FLUID_BB = new AxisAlignedBB(6/16D, 0, 6/16D, 10/16D, 1, 10/16D);

    private final ModelRenderer model;

    public RenderSpawnerExtractor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        model = new ModelRenderer(64, 64, 0, 0);
        model.setPos(0.0F, 24.0F, 0.0F);
        model.texOffs(23, 57).addBox(-3.0F, -16.25F, -3.0F, 6.0F, 1.0F, 6.0F, -0.01F, false);
        model.texOffs(23, 57).addBox(-3.0F, -14.75F, -3.0F, 6.0F, 1.0F, 6.0F, -0.01F, true);
        model.texOffs(44, 57).addBox(-2.5F, -15.5F, -2.5F, 5.0F, 1.0F, 5.0F, -0.01F, false);
        model.texOffs(15, 46).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 16.0F, 2.0F, 0.0F, false);
    }

    // TODO: Add Memory Essence Liquid Render, if you still want to do so
    @Override
    void renderModel(TileEntitySpawnerExtractor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_SPAWNER_EXTRACTOR));

        float extension = te.getProgress() * -0.75f;

        matrixStackIn.translate(0, extension, 0);
        model.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }

    @Override
    protected void renderExtras(TileEntitySpawnerExtractor te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getProgress() > 0f && te.getProgress() < 1f) {
            matrixStack.pushPose();
            matrixStack.translate(0, 13/16D, 0);
            IVertexBuilder builder = iRenderTypeBuffer.getBuffer(RenderType.entityTranslucentCull(AtlasTexture.LOCATION_BLOCKS));
            TankRenderInfo info = new TankRenderInfo(new FluidStack(ModFluids.MEMORY_ESSENCE.get(), (int) (1000 * te.getProgress())), 1000, FLUID_BB);
            AbstractFluidTER.renderFluid(builder, info, matrixStack.last().pose(), combinedLightIn, combinedOverlayIn);
            matrixStack.popPose();
        }
    }
}
