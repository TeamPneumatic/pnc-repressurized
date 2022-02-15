package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderDrone extends MobRenderer<AbstractDroneEntity, ModelDrone> {
    private final ResourceLocation texture;
    private final float scale;

    private RenderDrone(EntityRendererProvider.Context ctx, float scale, ResourceLocation texture) {
        super(ctx, new ModelDrone(ctx.bakeLayer(PNCModelLayers.DRONE)), 0.25f);

        this.scale = scale;
        this.texture = texture;

        addLayer(new DroneColourLayer(this));
        addLayer(new DroneHeldItemLayer(this));
        addLayer(new DroneDigLaserLayer(this));
        addLayer(new DroneMinigunLayer(this));
        addLayer(new DroneTargetLaserLayer(this));
    }

    public static RenderDrone standard(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.DRONE_ENTITY);
    }

    public static RenderDrone programmableController(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.25f, Textures.DRONE_ENTITY);
    }

    public static RenderDrone logistics(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.LOGISTICS_DRONE_ENTITY);
    }

    public static RenderDrone harvesting(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.HARVESTING_DRONE_ENTITY);
    }

    public static RenderDrone guard(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.GUARD_DRONE_ENTITY);
    }

    public static RenderDrone collector(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.COLLECTOR_DRONE_ENTITY);
    }

    public static RenderDrone amadrone(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.AMADRONE_ENTITY);
    }

    @Override
    public void render(AbstractDroneEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
//        matrixStackIn.translate(entityIn.getWidth() / 2, entityIn.getHeight() / 2, entityIn.getWidth() / 2);
        matrixStackIn.scale(scale, scale, scale);
//        matrixStackIn.translate(-entityIn.getWidth() / 2, -entityIn.getHeight() / 2, -entityIn.getWidth() / 2);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractDroneEntity entity) {
        return texture;
    }
}
