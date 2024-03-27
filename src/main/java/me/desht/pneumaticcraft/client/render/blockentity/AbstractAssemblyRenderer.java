package me.desht.pneumaticcraft.client.render.blockentity;

import me.desht.pneumaticcraft.common.block.entity.AbstractAssemblyRobotBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public abstract class AbstractAssemblyRenderer<T extends AbstractAssemblyRobotBlockEntity> extends AbstractBlockEntityModelRenderer<T> {
    AbstractAssemblyRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2
        );
    }
}
