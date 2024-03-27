package me.desht.pneumaticcraft.common.block.entity;

import me.desht.pneumaticcraft.api.pressure.PressureTier;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class ManualCompressorBlockEntity extends AbstractAirHandlingBlockEntity {
    public static final int TICKS_PER_PUMP_STEP = 4; // Should be multiple of 4 to match hold-right click frequency
    public int ticksUntilNextPumpStep = 0;
    private final static int airPerPumpCycle = ConfigHelper.common().machines.manualCompressorAirPerCycle.get();
    private final int effectiveVolume = airHandler.getVolume() * 5;
    @DescSynced
    public int pumpCycleProgress = 0;

    // Used to determine how difficult each pump cycle step is
    // Needs to be synced to client for pumprod rendering
    @DescSynced
    public int storedAir = 0;

    // Pumprod rendering fields
    public double pumprodVerticalOffsetCurrent = 0;
    public double pumprodVerticalOffsetPrevious = 0;
    public double pumprodVerticalOffsetCurrentTick = 0;
    public double pumprodVerticalOffsetPreviousTick = 0;
    public long pumpStepStartTick = 0;

    ManualCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PressureTier tier, int volume) {
        super(type, pos, state, tier, volume, 0);
    }

    public ManualCompressorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntityTypes.MANUAL_COMPRESSOR.get(), pos, state, PressureTier.TIER_ONE, PneumaticValues.VOLUME_MANUAL_COMPRESSOR);
    }

    @Override
    public boolean hasItemCapability() {
        return false;
    }

    @Override
    public void tickClient() {
        super.tickClient();

        // Updates pumprod render variables
        if (level.getGameTime() <= pumpStepStartTick + ManualCompressorBlockEntity.TICKS_PER_PUMP_STEP) {
            int currentPumpStepTick = Math.abs(ticksUntilNextPumpStep - TICKS_PER_PUMP_STEP);
            double offsetPerTick = (pumprodVerticalOffsetCurrent - pumprodVerticalOffsetPrevious) / TICKS_PER_PUMP_STEP;
            pumprodVerticalOffsetCurrentTick = pumprodVerticalOffsetPrevious + (offsetPerTick * currentPumpStepTick);
            pumprodVerticalOffsetPreviousTick = pumprodVerticalOffsetPrevious + (offsetPerTick * (currentPumpStepTick - 1));
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();

        // Only syncs storedAir when necessary:
        //  Since progressToAdd rounds after dividing by 500, only every 250 storedAir will have any effect anyway
        //  storedAir also needs to sync if it has changed more than 500
        if ((storedAir != airHandler.getAir() && airHandler.getAir() % 250 == 0)
                || Math.abs(storedAir - airHandler.getAir()) > 500) {
            storedAir = airHandler.getAir();
        }
    }

    @Override
    public void tickCommonPre() {
        super.tickCommonPre();

        // Pump step cooldown
        if (ticksUntilNextPumpStep != 0) {
            ticksUntilNextPumpStep--;
        }
    }

    /**
     * Updates the pumprod vertical offset based on the current pump cycle progress
     * Only affects rendering
     */
    public void setPumprodVerticalOffset() {
        // Changes the position of the pumprod relative to the pump cycle's progress
        // 0% = 0, 100% = 6.75
        double pumprodVerticalOffset = pumpCycleProgress / (400/27.0);

        // Updates previous and then current offsets
        if (pumprodVerticalOffsetPrevious != pumprodVerticalOffset) {
            pumprodVerticalOffsetPrevious = this.pumprodVerticalOffsetCurrent;
            this.pumprodVerticalOffsetCurrent = pumprodVerticalOffset;
        }

        pumpStepStartTick = level.getGameTime();
    }

    /**
     * Triggered on right click of compressor
     * <br>
     * Adds to pump cycle progress, along with reducing player hunger
     */
    public void onPumpCycleStep(Player player) {
        // Limits pump cycle step frequency to prevent auto clicker abuse
        if (ticksUntilNextPumpStep == 0) {
            float hungerDrain = ConfigHelper.common().machines.manualCompressorHungerDrainPerCycleStep.get().floatValue();

            // Normal pump cycle steps
            if (pumpCycleProgress != 100) {
                // Pump cycles take more pumps the more full the compressor is
                // When empty: +50 (2 pumps)
                // When full: +0 (infinite pumps)
                int progressToAdd = Math.max((int) Math.round((effectiveVolume - storedAir) / 500.0), 0);
                pumpCycleProgress = Math.min(pumpCycleProgress + progressToAdd, 100);

                // Drains players hunger per cycle step
                player.causeFoodExhaustion(hungerDrain);

                // pumpCycleProgress acts as a percent out of 100, with 100 meaning the cycle is complete
                if (pumpCycleProgress == 100) {
                    onPumpCycleComplete();
                }
            }

            // Adds an extra step for pulling the pumprod up from the bottom
            // Pulling it up takes twice as much hunger
            else {
                pumpCycleProgress = 0;
                player.causeFoodExhaustion(hungerDrain * 2);
            }

            setPumprodVerticalOffset();

            // Resets the ticks until next pump step at the end of the current pump step
            ticksUntilNextPumpStep = TICKS_PER_PUMP_STEP;
        }
    }

    /**
     * Triggered on completion of a pump
     * <br>
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
    public IItemHandler getItemHandler(@Nullable Direction dir) {
        return null;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return getRotation() == side;
    }
}
