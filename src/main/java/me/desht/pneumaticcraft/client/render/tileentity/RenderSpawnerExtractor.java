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
import me.desht.pneumaticcraft.common.tileentity.TileEntitySpawnerExtractor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderSpawnerExtractor extends AbstractTileModelRenderer<TileEntitySpawnerExtractor> {
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
}
