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
    public static final IRenderFactory<EntityDroneBase> REGULAR_FACTORY = manager -> new RenderDrone(manager, Textures.DRONE_ENTITY);
    public static final IRenderFactory<EntityDroneBase> PROGRAMMABLE_CONTROLLER_FACTORY = manager -> new RenderDrone(manager, Textures.DRONE_ENTITY, 0.25f);
    public static final IRenderFactory<EntityDroneBase> LOGISTICS_FACTORY = manager -> new RenderDrone(manager, Textures.LOGISTICS_DRONE_ENTITY);
    public static final IRenderFactory<EntityDroneBase> HARVESTING_FACTORY = manager -> new RenderDrone(manager, Textures.HARVESTING_DRONE_ENTITY);
    public static final IRenderFactory<EntityDroneBase> GUARD_FACTORY = manager -> new RenderDrone(manager, Textures.GUARD_DRONE_ENTITY);
    public static final IRenderFactory<EntityDroneBase> COLLECTOR_FACTORY = manager -> new RenderDrone(manager, Textures.COLLECTOR_DRONE_ENTITY);
    public static final IRenderFactory<EntityDroneBase> AMADRONE_FACTORY = manager -> new RenderDrone(manager, Textures.AMADRONE_ENTITY);

    private final ResourceLocation texture;

    private final float scale;

    private RenderDrone(EntityRendererManager entityRendererManager, ResourceLocation texture, float scale) {
        super(entityRendererManager, new ModelDrone(), 0f);

        this.scale = scale;
        this.texture = texture;

        addLayer(new DroneColourLayer(this));
        addLayer(new DroneHeldItemLayer(this));
        addLayer(new DroneDigLaserLayer(this));
        addLayer(new DroneMinigunLayer(this));
        addLayer(new DroneTargetLaserLayer(this));
    }

    private RenderDrone(EntityRendererManager manager, ResourceLocation texture) {
        this(manager,  texture, 0.35f);
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
        return texture;
    }
}
