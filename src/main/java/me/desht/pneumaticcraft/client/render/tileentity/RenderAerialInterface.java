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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAerialInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.GenericHeadModel;
import net.minecraft.client.renderer.entity.model.HumanoidHeadModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;

import java.util.Map;

public class RenderAerialInterface extends TileEntityRenderer<TileEntityAerialInterface> {
    private final GenericHeadModel headModel = new HumanoidHeadModel();

    private static final double EXTRUSION = 0.05;  // how far the head sticks out of the main block

    public RenderAerialInterface(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TileEntityAerialInterface tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        // code adapted from SkullTileEntityRenderer
        if (tileEntityIn.gameProfileClient != null) {
            GameProfile gameProfile = tileEntityIn.gameProfileClient;
            Direction dir = tileEntityIn.getRotation().getOpposite();
            float rotation = 90F * dir.get2DDataValue();
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            Map<Type, MinecraftProfileTexture> map = skinManager.getInsecureSkinInformation(gameProfile);
            RenderType renderType = map.containsKey(Type.SKIN) ?
                    RenderType.entityTranslucent(skinManager.registerTexture(map.get(Type.SKIN), Type.SKIN)) :
                    RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(PlayerEntity.createPlayerUUID(gameProfile)));
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5 - dir.getStepX() * (0.25 + EXTRUSION), 0.25D, 0.5 - dir.getStepZ() * (0.25 + EXTRUSION));
            matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
            IVertexBuilder builder = bufferIn.getBuffer(renderType);
            headModel.setupAnim(0F, rotation, 0F);  // setRotations?
            headModel.renderToBuffer(matrixStackIn, builder, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStackIn.popPose();
        }
    }

    /*
     * For future re-texture plans: render head on top of Aerial Interface, facing up
     */

//    @Override
//    public void render(TileEntityAerialInterface tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
//        // code adapted from SkullTileEntityRenderer
//        if (tileEntityIn.gameProfileClient != null) {
//            GameProfile gameProfile = tileEntityIn.gameProfileClient;
//            Direction dir = tileEntityIn.getRotation();
//            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
//            Map<Type, MinecraftProfileTexture> map = skinManager.loadSkinFromCache(gameProfile);
//            RenderType renderType = map.containsKey(Type.SKIN) ?
//                    RenderType.getEntityTranslucent(skinManager.loadSkin(map.get(Type.SKIN), Type.SKIN)) :
//                    RenderType.getEntityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(PlayerEntity.getUUID(gameProfile)));
//            matrixStackIn.push();
//            matrixStackIn.translate(0.5 + dir.getXOffset() * 0.25, 0.5D + (0.25 + EXTRUSION), 0.5 + dir.getZOffset() * 0.25);
//            matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
//            IVertexBuilder builder = bufferIn.getBuffer(renderType);
//            headModel.setupAnim(0F, dir.getOpposite().getHorizontalIndex() * 90F, -90F);  // setRotations?
//            headModel.render(matrixStackIn, builder, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//            matrixStackIn.pop();
//        }
//    }
}
