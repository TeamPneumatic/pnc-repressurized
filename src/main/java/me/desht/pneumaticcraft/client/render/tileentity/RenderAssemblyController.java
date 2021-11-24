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
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3f;

public class RenderAssemblyController extends AbstractTileModelRenderer<TileEntityAssemblyController> {
    private static final float TEXT_SIZE = 0.01F;
    private final ModelRenderer screen;

    public RenderAssemblyController(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        screen = new ModelRenderer(64, 64, 33, 32);
        screen.addBox(0F, 0F, 0F, 10, 6, 1);
        screen.setPos(-5F, 8F, 1F);
        screen.mirror = true;
        screen.xRot = -0.5934119F;
    }

    @Override
    public void renderModel(TileEntityAssemblyController te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_ASSEMBLY_CONTROLLER));

        // have the screen face the player
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180 + Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        screen.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        // status text
        matrixStackIn.translate(-0.25D, 0.53D, 0.04D);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(-34));
        matrixStackIn.scale(TEXT_SIZE, TEXT_SIZE, TEXT_SIZE);
        Minecraft.getInstance().font.drawInBatch(te.displayedText, 1, 4, 0xFFFFFFFF, false,  matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);

        // possible problem icon
        if (te.hasProblem) {
            RenderUtils.drawTexture(matrixStackIn, bufferIn.getBuffer(ModRenderTypes.getTextureRenderColored(Textures.GUI_PROBLEMS_TEXTURE)), 28, 12, combinedLightIn);
        }
    }
}
