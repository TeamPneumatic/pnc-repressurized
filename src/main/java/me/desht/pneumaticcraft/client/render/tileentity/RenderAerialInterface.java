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
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            Map<Type, MinecraftProfileTexture> map = skinManager.getInsecureSkinInformation(gameProfile);
            RenderType renderType = map.containsKey(Type.SKIN) ?
                    RenderType.entityTranslucent(skinManager.registerTexture(map.get(Type.SKIN), Type.SKIN)) :
                    RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(PlayerEntity.createPlayerUUID(gameProfile)));
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5 + dir.getStepX() * 0.25, 0.5D + (0.25 + EXTRUSION), 0.5 + dir.getStepZ() * 0.25);
            matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
            IVertexBuilder builder = bufferIn.getBuffer(renderType);
            headModel.setupAnim(0F, dir.getOpposite().get2DDataValue() * 90F, -90F);  // setRotations?
            headModel.renderToBuffer(matrixStackIn, builder, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStackIn.popPose();
        }
    }
}
