package me.desht.pneumaticcraft.client.render.entity;

import me.desht.pneumaticcraft.client.model.entity.ModelDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

public class RenderDrone extends RenderLiving<EntityDroneBase> {
//    private final ModelDrone model;

    public static final IRenderFactory<EntityDroneBase> REGULAR_FACTORY = manager -> new RenderDrone(manager, false);
    public static final IRenderFactory<EntityDroneBase> LOGISTICS_FACTORY = manager -> new RenderDrone(manager, true);

    public RenderDrone(RenderManager manager, boolean isLogisticsDrone) {
        super(manager, new ModelDrone(isLogisticsDrone), 0);
//        model = new ModelDrone(isLogisticsDrone);
    }

    private void renderDrone(EntityDroneBase drone, double x, double y, double z, float yaw, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.76F, 0);
        GL11.glScalef(0.5F, -0.5F, -0.5F);
        bindEntityTexture(drone);
        mainModel.setLivingAnimations(drone, 0, 0, partialTicks);
        mainModel.render(drone, 0, 0, 0, 0, partialTicks, 1 / 16F);
        GL11.glPopMatrix();

        drone.renderExtras(x, y, z, partialTicks);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDroneBase par1Entity) {
        return Textures.MODEL_DRONE;
    }

    @Override
    public void doRender(EntityDroneBase drone, double par2, double par4, double par6, float par8, float par9) {
        renderDrone(drone, par2, par4, par6, par8, par9);
        renderName(drone, par2, par4, par6); //TODO 1.8 test (renaming)
    }

    @Override
    protected boolean canRenderName(EntityDroneBase drone) {
        return super.canRenderName(drone) && (drone.getAlwaysRenderNameTagForRender() || drone.hasCustomName() && drone == renderManager.pointedEntity);
    }
}
