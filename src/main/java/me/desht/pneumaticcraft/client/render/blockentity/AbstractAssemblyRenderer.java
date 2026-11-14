package me.desht.pneumaticcraft.client.render.blockentity;

import me.desht.pneumaticcraft.common.block.entity.processing.AbstractAssemblyRobotBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractAssemblyRenderer<T extends AbstractAssemblyRobotBlockEntity, S extends AbstractAssemblyRenderer.AssemblyRobotState> extends AbstractBlockEntityModelRenderer<T, S> {
    AbstractAssemblyRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (state.angles.length != blockEntity.angles.length) {
            state.angles = new float[blockEntity.angles.length];
        }
        for (int i = 0; i < state.angles.length; i++) {
            state.angles[i] = Mth.lerp(partialTicks, blockEntity.oldAngles[i], blockEntity.angles[i]);
        }
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2
        );
    }

    public static class AssemblyRobotState extends ModelRenderState {
        float[] angles = new float[0];
    }
}
