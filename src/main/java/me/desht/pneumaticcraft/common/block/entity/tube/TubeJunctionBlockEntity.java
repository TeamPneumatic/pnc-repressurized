package me.desht.pneumaticcraft.common.block.entity.tube;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class TubeJunctionBlockEntity extends AbstractAirHandlingBlockEntity {
    private final IAirHandlerMachine tube2Handler;

    public TubeJunctionBlockEntity(BlockPos pPos, BlockState pState) {
        super(ModBlockEntityTypes.TUBE_JUNCTION.get(), pPos, pState, PressureTier.TIER_TWO, 4000, 0);

        this.tube2Handler  = new MachineAirHandler(PressureTier.TIER_TWO, 4000);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Nullable
    @Override
    public IAirHandlerMachine getAirHandler(Direction dir) {
        if (dir != null) {
            Axis axis = getBlockState().getValue(BlockStateProperties.AXIS);
            if (axis == Axis.X && dir.getAxis() == Axis.Y
                    || axis == Axis.Y && dir.getAxis() == Axis.Z
                    || axis == Axis.Z && dir.getAxis() == Axis.X) {
                return tube2Handler;
            }
        }
        return super.getAirHandler(dir);
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis() != getBlockState().getValue(BlockStateProperties.AXIS);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("tube2", tube2Handler.serializeNBT());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        tube2Handler.deserializeNBT(tag.getCompound("tube2"));
    }
}
