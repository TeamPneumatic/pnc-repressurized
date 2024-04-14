package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.entity.utility.AerialInterfaceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.Direction;

public class AerialInterfaceRenderer implements BlockEntityRenderer<AerialInterfaceBlockEntity> {
    private final SkullModel headModel;

    private static final double EXTRUSION = 0.05;  // how far the head sticks out of the main block

    public AerialInterfaceRenderer(BlockEntityRendererProvider.Context ctx) {
        headModel = new SkullModel(ctx.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    @Override
    public void render(AerialInterfaceBlockEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        // code adapted from SkullTileEntityRenderer
        if (tileEntityIn.gameProfileClient != null) {
            GameProfile gameProfile = tileEntityIn.gameProfileClient;
            Direction dir = tileEntityIn.getRotation();
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            PlayerSkin playerSkin = skinManager.getInsecureSkin(gameProfile);
            RenderType renderType = RenderType.entityTranslucent(playerSkin.texture());

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5 + dir.getStepX() * 0.25, 0.5D + (0.25 + EXTRUSION), 0.5 + dir.getStepZ() * 0.25);
            matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
            VertexConsumer builder = bufferIn.getBuffer(renderType);
            headModel.setupAnim(0F, dir.getOpposite().get2DDataValue() * 90F, -90F);  // setRotations?
            headModel.renderToBuffer(matrixStackIn, builder, RenderUtils.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
            matrixStackIn.popPose();
        }
    }
}
