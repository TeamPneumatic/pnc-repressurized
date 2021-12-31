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
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderPneumaticDoor extends AbstractTileModelRenderer<TileEntityPneumaticDoor> {
    private final ModelRenderer door;
    // TODO: Allow the door to be placed left or right, like Vanilla doors
    public RenderPneumaticDoor(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);

        door = new ModelRenderer(64, 64, 0, 0);
        door.setPos(-8.0F, 3.0F, -8.0F);
        door.texOffs(0, 0).addBox(0.0F, -11.0F, 0.0F, 16.0F, 32.0F, 3.0F, 0.0F, true);
        door.texOffs(42, 2).addBox(1.0F, -11.0F, 2.25F, 1.0F, 32.0F, 1.0F, -0.01F, false);
        door.texOffs(38, 2).addBox(3.0F, -11.0F, 2.25F, 1.0F, 32.0F, 1.0F, -0.01F, false);
        door.texOffs(38, 2).addBox(3.0F, -11.0F, -0.25F, 1.0F, 32.0F, 1.0F, -0.01F, false);
        door.texOffs(42, 2).addBox(1.0F, -11.0F, -0.25F, 1.0F, 32.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 46).addBox(0.0F, -9.0F, 2.5F, 5.0F, 1.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 44).addBox(0.0F, 18.0F, 2.5F, 5.0F, 1.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 46).addBox(0.0F, -9.0F, -0.5F, 5.0F, 1.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 44).addBox(0.0F, 18.0F, -0.5F, 5.0F, 1.0F, 1.0F, -0.01F, false);
        door.texOffs(16, 35).addBox(0.5F, 1.0F, 3.0F, 4.0F, 8.0F, 1.0F, 0.0F, false);
        door.texOffs(16, 35).addBox(0.5F, 1.0F, -1.0F, 4.0F, 8.0F, 1.0F, 0.0F, false);
        door.texOffs(26, 35).addBox(1.5F, 2.0F, 4.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
        door.texOffs(26, 35).addBox(1.5F, 2.0F, -2.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);
        door.texOffs(26, 38).addBox(2.5F, 2.5F, 4.0F, 4.0F, 1.0F, 1.0F, -0.2F, false);
        door.texOffs(26, 38).addBox(2.5F, 2.5F, -2.0F, 4.0F, 1.0F, 1.0F, -0.2F, false);
        door.texOffs(0, 41).addBox(9.0F, -8.0F, 2.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 38).addBox(9.0F, 9.0F, 2.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 35).addBox(9.0F, 16.0F, 2.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 41).addBox(9.0F, -8.0F, -0.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 38).addBox(9.0F, 9.0F, -0.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
        door.texOffs(0, 35).addBox(9.0F, 16.0F, -0.25F, 7.0F, 2.0F, 1.0F, -0.01F, false);
    }

    @Override
    public void renderModel(TileEntityPneumaticDoor te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (te.getBlockState().getValue(BlockPneumaticDoor.TOP_DOOR)) return;

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_PNEUMATIC_DOOR_DYNAMIC));

        float rotation = MathHelper.lerp(partialTicks, te.oldRotationAngle, te.rotationAngle);
        boolean rightGoing = te.rightGoing;
        float[] rgb = DyeColor.byId(te.color).getTextureDiffuseColors();

        RenderUtils.rotateMatrixForDirection(matrixStackIn, te.getRotation());

        matrixStackIn.translate((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        matrixStackIn.mulPose(rightGoing ? Vector3f.YN.rotationDegrees(rotation) : Vector3f.YP.rotationDegrees(rotation));
        matrixStackIn.translate((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);

        door.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn, rgb[0], rgb[1], rgb[2], 1f);
    }
}
