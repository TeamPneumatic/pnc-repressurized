package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.core.ModBlockEntities;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public class ManualCompressorBlockEntity extends AbstractAirHandlingBlockEntity {
    private final int airPerPumpCycle = PneumaticValues.PRODUCTION_MANUAL_COMPRESSOR;
    private final int effectiveVolume = airHandler.getVolume() * 5;
    public int pumpCycleProgress = 0;

    // Used to determine how difficult each pump cycle step is
    // Needs to be synced to client for pumpbar rendering
    @DescSynced
    public int storedAir = 0;

    ManualCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volume) {
        super(type, pos, state, tier, volume, 0);
    }

    public ManualCompressorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MANUAL_COMPRESSOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_MANUAL_COMPRESSOR);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        storedAir = airHandler.getAir();
    }

    /**
     * Triggered on right click of compressor
     * Adds to pump cycle progress
     */
    public void onPumpCycle() {
        // Normal pump cycle steps
        if (pumpCycleProgress != 100) {
            // Pump cycles take more pumps the more full the compressor is
            // When empty: +50 (2 pumps)
            // When full: +0 (infinite pumps)
            int progressToAdd = Math.max((int) Math.round((effectiveVolume - storedAir) / 500.0), 0);
            pumpCycleProgress = Math.min(pumpCycleProgress + progressToAdd, 100);

            // pumpCycleProgress acts as a percent out of 100, with 100 meaning the cycle is complete
            if (pumpCycleProgress == 100) {
                onPumpCycleComplete();
            }
        }

        // Adds an extra step for pulling the pumprod up from the bottom
        else {
            pumpCycleProgress = 0;
        }
    }

    /**
     * Triggered on completion of a pump
     * Adds compressed air from pump and resets pump cycle
     */
    public void onPumpCycleComplete() {
        // Adds just enough air to completely fill the compressor if adding the default amount
        // would cause the compressor to overfill
        if (airHandler.getAir() + airPerPumpCycle > effectiveVolume)
        {
            addAir(effectiveVolume - airHandler.getAir());
        }

        // Adds the default amount of air
        else {
            addAir(airPerPumpCycle);
        }

        // Plays sound to denote a finished cycle
        level.playSound(null, getBlockPos(), ModSounds.SHORT_HISS.get(), SoundSource.BLOCKS, 1 ,1);
    }

    @Override
    public IItemHandler getPrimaryInventory() {
        return null;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }
}
