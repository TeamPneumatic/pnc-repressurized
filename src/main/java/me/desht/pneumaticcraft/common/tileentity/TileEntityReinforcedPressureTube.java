package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityReinforcedPressureTube extends TileEntityPressureTube {
    public TileEntityReinforcedPressureTube(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REINFORCED_PRESSURE_TUBE.get(), pos, state, PressureTier.TIER_ONE_HALF, PneumaticValues.VOLUME_PRESSURE_TUBE, 0);
    }
}
