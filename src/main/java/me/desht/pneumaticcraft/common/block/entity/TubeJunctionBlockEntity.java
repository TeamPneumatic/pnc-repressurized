package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.capabilities.MachineAirHandler;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TubeJunctionBlockEntity extends AbstractAirHandlingBlockEntity {
    private final IAirHandlerMachine tube2Handler;
    private LazyOptional<IAirHandlerMachine> tube2Cap;

    public TubeJunctionBlockEntity(BlockPos pPos, BlockState pState) {
        super(ModBlockEntities.TUBE_JUNCTION.get(), pPos, pState, PressureTier.TIER_TWO, 4000, 0);

        this.tube2Handler  = new MachineAirHandler(PressureTier.TIER_TWO, 4000);
        this.tube2Cap = LazyOptional.of(() -> tube2Handler);
    }

    @Override
    public void invalidateCaps() {
        this.tube2Cap.invalidate();
        this.tube2Cap = LazyOptional.empty();
        super.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.tube2Cap = LazyOptional.of(() -> tube2Handler);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY && side != null) {
            Axis axis = getBlockState().getValue(BlockStateProperties.AXIS);
            if (axis == Axis.X && side.getAxis() == Axis.Y
                || axis == Axis.Y && side.getAxis() == Axis.Z
                || axis == Axis.Z && side.getAxis() == Axis.X) {
                return tube2Cap.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side.getAxis() != getBlockState().getValue(BlockStateProperties.AXIS);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("tube2", tube2Handler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tube2Handler.deserializeNBT(tag.getCompound("tube2"));
    }
}
