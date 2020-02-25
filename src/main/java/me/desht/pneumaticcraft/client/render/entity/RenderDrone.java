package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.entity.ModelDrone;
import me.desht.pneumaticcraft.client.render.RenderDroneHeldItem;
import me.desht.pneumaticcraft.client.render.RenderLaser;
import me.desht.pneumaticcraft.client.render.RenderMinigunTracers;
import me.desht.pneumaticcraft.client.render.RenderProgressingLine;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.item.ItemGunAmmo;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderDrone extends MobRenderer<EntityDroneBase,ModelDrone> {
    public static final IRenderFactory<EntityDroneBase> REGULAR_FACTORY = RenderDrone::new;
    public static final IRenderFactory<EntityDroneBase> LOGISTICS_FACTORY = manager -> new RenderDrone(manager, 0xFFFF0000);
    public static final IRenderFactory<EntityDroneBase> HARVESTING_FACTORY = manager -> new RenderDrone(manager, 0xFF006102);
    public static final IRenderFactory<EntityDroneBase> AMADRONE_FACTORY = manager -> new RenderDrone(manager, 0xFFFF8000);

    private final RenderLaser laserRenderer = new RenderLaser();
    private final RenderDroneHeldItem heldItemRenderer = new RenderDroneHeldItem();
    private RenderMinigunTracers minigunTracersRenderer;

    private RenderDrone(EntityRendererManager manager) {
        super(manager, new ModelDrone(), 0);
    }
    
    private RenderDrone(EntityRendererManager manager, int frameColor) {
        super(manager, new ModelDrone(frameColor), 0);
    }

    private void renderDrone(EntityDroneBase drone, double x, double y, double z, float yaw, float partialTicks) {
        if (drone.getHealth() <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translated((float) x, (float) y, (float) z);

        GlStateManager.pushMatrix();
        GlStateManager.translated(0, 0.76F, 0);
        GlStateManager.scaled(0.5F, -0.5F, -0.5F);
        bindEntityTexture(drone);
        getEntityModel().setLivingAnimations(drone, 0, 0, partialTicks);
        getEntityModel().render(drone, 0, 0, 0, 0, partialTicks, 1 / 16F);
        GlStateManager.popMatrix();

        renderExtras(drone, x, y, z, partialTicks);

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDroneBase par1Entity) {
        return Textures.MODEL_DRONE;
    }

    @Override
    public void doRender(EntityDroneBase drone, double par2, double par4, double par6, float par8, float par9) {
        renderDrone(drone, par2, par4, par6, par8, par9);
        renderName(drone, par2, par4, par6);
    }

    @Override
    protected boolean canRenderName(EntityDroneBase drone) {
        return super.canRenderName(drone) && (drone.getAlwaysRenderNameTagForRender() || drone.hasCustomName() && drone == renderManager.pointedEntity);
    }

    private void renderExtras(EntityDroneBase droneBase, double x, double y, double z, float partialTicks) {
        BlockPos diggingPos = droneBase.getDugBlock();
        if (diggingPos != null) {
            laserRenderer.render(partialTicks, droneBase,
                    0, droneBase.getLaserOffsetY(), 0,
                    diggingPos.getX() + 0.5 - droneBase.posX, diggingPos.getY() + 0.45 - droneBase.posY, diggingPos.getZ() + 0.5 - droneBase.posZ);
        }

        if (droneBase instanceof EntityDrone) {
            EntityDrone drone = (EntityDrone) droneBase;
            renderTargetLine(drone, partialTicks);

            if (drone.hasMinigun()) {
                renderMinigunTracers(drone, partialTicks);
            }

            ItemStack held = drone.getDroneHeldItem();
            if (!held.isEmpty() && !(held.getItem() instanceof ItemGunAmmo && drone.hasMinigun())) {
                heldItemRenderer.render(held);
            }
        }
    }

    private void renderTargetLine(EntityDrone drone, float partialTicks) {
        RenderProgressingLine targetLine = drone.getTargetLine();
        RenderProgressingLine oldTargetLine = drone.getOldTargetLine();
        if (targetLine != null && oldTargetLine != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scaled(1, -1, 1);
            GlStateManager.disableTexture();
            GlStateManager.color4f(1, 0, 0, 1);
            targetLine.renderInterpolated(oldTargetLine, partialTicks);
            GlStateManager.color4f(1, 1, 1, 1);
            GlStateManager.enableTexture();
            GlStateManager.popMatrix();
        }
    }

    private void renderMinigunTracers(EntityDrone drone, float partialTicks) {
        if (minigunTracersRenderer == null) {
            minigunTracersRenderer = new RenderMinigunTracers(drone.getMinigun());
        }
        double x1 = drone.lastTickPosX + (drone.posX - drone.lastTickPosX) * partialTicks;
        double y1 = drone.lastTickPosY + (drone.posY - drone.lastTickPosY) * partialTicks;
        double z1 = drone.lastTickPosZ + (drone.posZ - drone.lastTickPosZ) * partialTicks;
        minigunTracersRenderer.render(x1, y1, z1, 0.6);
    }
}
