package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.util.ProgressingLine;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderDrone extends MobRenderer<AbstractDroneEntity, DroneRenderState, ModelDrone> {
    private final Identifier texture;
    private final float scale;

    private RenderDrone(EntityRendererProvider.Context ctx, float scale, Identifier texture) {
        super(ctx, new ModelDrone(ctx.bakeLayer(PNCModelLayers.DRONE)), 0.25f);

        this.scale = scale;
        this.texture = texture;

        addLayer(new DroneColourLayer(this, ctx));
        addLayer(new DroneHeldItemLayer(this));
        addLayer(new DroneDigLaserLayer(this));
        addLayer(new DroneMinigunLayer(this, ctx));
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
    public DroneRenderState createRenderState() {
        return new DroneRenderState();
    }

    @Override
    public void extractRenderState(AbstractDroneEntity drone, DroneRenderState state, float partialTicks) {
        super.extractRenderState(drone, state, partialTicks);

        state.propRotation = Mth.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        state.droneColor = 0xFF000000 | DyeColor.byId(drone.getDroneColor()).getTextureDiffuseColor();

        extractHeldItem(drone, state);
        extractDigLaser(drone, state);
        state.laserColor = drone.getLaserColor();

        state.targetLine = null;
        state.hasMinigun = false;
        if (drone instanceof DroneEntity droneEntity) {
            state.targetLine = getTargetLine(droneEntity.getOldTargetLine(), droneEntity.getTargetLine(), partialTicks);
            if (droneEntity.hasMinigun()) {
                state.hasMinigun = true;
                state.minigun.extract(droneEntity.getMinigun(), partialTicks);
            }
        }
    }

    private void extractHeldItem(AbstractDroneEntity drone, DroneRenderState state) {
        ItemStack held = drone.getDroneHeldItem();
        if (!held.isEmpty() && !(held.getItem() instanceof AbstractGunAmmoItem && drone.hasMinigun())) {
            itemModelResolver.updateForNonLiving(state.heldItem, held, ItemDisplayContext.FIXED, drone);
            // since items are rendered suspended under the drone,
            // holding tools upside down looks more natural - especially if the drone is digging with them
            state.heldItemUpsideDown = !(held.is(ItemTags.PICKAXES) || held.is(ItemTags.AXES) || held.is(ItemTags.SHOVELS)
                    || held.is(ItemTags.HOES) || held.is(ItemTags.SWORDS));
            state.heldItemScale = held.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        } else {
            state.heldItem.clear();
        }
    }

    private static void extractDigLaser(AbstractDroneEntity drone, DroneRenderState state) {
        BlockPos diggingPos = drone.getDugBlock();
        if (diggingPos != null) {
            BlockState blockState = drone.level().getBlockState(diggingPos);
            VoxelShape shape = blockState.getShape(drone.level(), diggingPos);
            Vec3 target = shape.isEmpty() ?
                    new Vec3(diggingPos.getX() + 0.5, diggingPos.getY() + 0.45, diggingPos.getZ() + 0.5) :
                    shape.bounds().getCenter().add(Vec3.atLowerCornerOf(diggingPos));
            state.digLaserStart = new Vec3(0, -drone.getLaserOffsetY(), 0);
            state.digLaserEnd = target.subtract(drone.getX(), drone.getY(), drone.getZ());
        } else {
            state.digLaserStart = null;
            state.digLaserEnd = null;
        }
    }

    private static float[] getTargetLine(ProgressingLine prev, ProgressingLine line, float partialTick) {
        if (prev == null || line == null) {
            return null;
        }
        float progress = line.getProgress();
        float lx1 = Mth.lerp(partialTick, line.startX, prev.startX);
        float ly1 = Mth.lerp(partialTick, line.startY, prev.startY);
        float lz1 = Mth.lerp(partialTick, line.startZ, prev.startZ);
        float lx2 = Mth.lerp(progress, lx1, Mth.lerp(partialTick, line.endX, prev.endX));
        float ly2 = Mth.lerp(progress, ly1, Mth.lerp(partialTick, line.endY, prev.endY));
        float lz2 = Mth.lerp(progress, lz1, Mth.lerp(partialTick, line.endZ, prev.endZ));
        return new float[] { lx1, ly1, lz1, lx2, ly2, lz2 };
    }

    @Override
    public void submit(DroneRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(DroneRenderState state) {
        return texture;
    }
}
