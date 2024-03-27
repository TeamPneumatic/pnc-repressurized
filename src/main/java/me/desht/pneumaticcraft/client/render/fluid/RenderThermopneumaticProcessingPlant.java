package me.desht.pneumaticcraft.client.render.fluid;

import me.desht.pneumaticcraft.common.block.entity.ThermoPlantBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.Collection;
import java.util.List;

public class RenderThermopneumaticProcessingPlant extends AbstractFluidTER<ThermoPlantBlockEntity> {
    private static final AABB TANK_BOUNDS_1 = new AABB(9.1 / 16f, 1 / 16f, 0.1 / 16f, 15.9 / 16f, 11.9 / 16f, 4.9 / 16f);
    private static final AABB TANK_BOUNDS_2 = new AABB(0.1 / 16f, 1 / 16f, 0.1 / 16f, 6.9 / 16f, 11.9 / 16f, 4.9 / 16f);

    private static final AABB[] BOUNDS_IN = new AABB[4];
    static {
        BOUNDS_IN[0] = TANK_BOUNDS_1;
        BOUNDS_IN[1] = AbstractFluidTER.rotateY(BOUNDS_IN[0], 90);
        BOUNDS_IN[2] = AbstractFluidTER.rotateY(BOUNDS_IN[1], 90);
        BOUNDS_IN[3] = AbstractFluidTER.rotateY(BOUNDS_IN[2], 90);
    }
    private static final AABB[] BOUNDS_OUT = new AABB[4];
    static {
        BOUNDS_OUT[0] = TANK_BOUNDS_2;
        BOUNDS_OUT[1] = AbstractFluidTER.rotateY(BOUNDS_OUT[0], 90);
        BOUNDS_OUT[2] = AbstractFluidTER.rotateY(BOUNDS_OUT[1], 90);
        BOUNDS_OUT[3] = AbstractFluidTER.rotateY(BOUNDS_OUT[2], 90);
    }

    public RenderThermopneumaticProcessingPlant(BlockEntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    Collection<TankRenderInfo> getTanksToRender(ThermoPlantBlockEntity te) {
        return List.of(
                new TankRenderInfo(te.getInputTank(), BOUNDS_IN[te.getRotation().get2DDataValue()]).without(Direction.DOWN),
                new TankRenderInfo(te.getOutputTank(), BOUNDS_OUT[te.getRotation().get2DDataValue()]).without(Direction.DOWN)
        );
    }
}
