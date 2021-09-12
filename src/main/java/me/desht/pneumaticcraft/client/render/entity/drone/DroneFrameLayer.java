package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;

public class DroneFrameLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    private final float[] frameColors;

    private static final double FRAME_SIZE = 6 / 16D;
    private static final double FRAME_Y_OFFSET = 34 / 16D;
    private static final AxisAlignedBB FRAME_AABB = new AxisAlignedBB(
            -FRAME_SIZE, FRAME_Y_OFFSET - FRAME_SIZE, -FRAME_SIZE,
            FRAME_SIZE, FRAME_Y_OFFSET + FRAME_SIZE, FRAME_SIZE
    );

    DroneFrameLayer(RenderDrone renderer, int frameColor) {
        super(renderer);

        this.frameColors = RenderUtils.decomposeColorF(frameColor);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // transform is currently upside down; invert so the frame lighting looks right
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStackIn.translate(0, -2.12, 0);
        RenderUtils.renderFrame(matrixStackIn, bufferIn, FRAME_AABB, 1 / 32F, frameColors[1], frameColors[2], frameColors[3], 1f, packedLightIn, false);
        matrixStackIn.popPose();
    }

//    @Override
//    public ResourceLocation getEntityTexture(EntityDroneBase entity) {
//        return Textures.TEST_DRONE_ENTITY;
//    }
}
