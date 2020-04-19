package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderDrone extends MobRenderer<EntityDroneBase, ModelDrone> {
    public static final IRenderFactory<EntityDroneBase> REGULAR_FACTORY = manager -> new RenderDrone(manager, 0);
    public static final IRenderFactory<EntityDroneBase> PROGRAMMABLE_CONTROLLER_FACTORY = manager -> new RenderDrone(manager, 0, 0.25f);
    public static final IRenderFactory<EntityDroneBase> LOGISTICS_FACTORY = manager -> new RenderDrone(manager, 0xFFFF0000);
    public static final IRenderFactory<EntityDroneBase> HARVESTING_FACTORY = manager -> new RenderDrone(manager, 0xFF006102);
    public static final IRenderFactory<EntityDroneBase> GUARD_FACTORY = manager -> new RenderDrone(manager, 0xFF4B7FDE);
    public static final IRenderFactory<EntityDroneBase> COLLECTOR_FACTORY = manager -> new RenderDrone(manager, 0xFFCACA27);
    public static final IRenderFactory<EntityDroneBase> AMADRONE_FACTORY = manager -> new RenderDrone(manager, 0xFFFF8000);

    private final float scale;

    private RenderDrone(EntityRendererManager entityRendererManager, int frameColor, float scale) {
        super(entityRendererManager, new ModelDrone(), 0f);

        this.scale = scale;

        if ((frameColor & 0xFF000000) != 0) {
            addLayer(new DroneFrameLayer(this, frameColor));
        }
        addLayer(new DroneColourLayer(this));
        addLayer(new DroneHeldItemLayer(this));
        addLayer(new DroneDigLaserLayer(this));
        addLayer(new DroneMinigunLayer(this));
        addLayer(new DroneTargetLaserLayer(this));
    }

    private RenderDrone(EntityRendererManager manager, int frameColor) {
        this(manager,  frameColor, 0.5f);
    }

    @Override
    public void render(EntityDroneBase entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
//        matrixStackIn.translate(entityIn.getWidth() / 2, entityIn.getHeight() / 2, entityIn.getWidth() / 2);
        matrixStackIn.scale(scale, scale, scale);
//        matrixStackIn.translate(-entityIn.getWidth() / 2, -entityIn.getHeight() / 2, -entityIn.getWidth() / 2);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(EntityDroneBase entity) {
        return Textures.DRONE_ENTITY;
    }
}
