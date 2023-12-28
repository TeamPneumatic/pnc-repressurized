package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.common.block.entity.PressureTubeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

/**
 * Can be used by pressure tube modules to cache the air handler capability of the neighbouring tube
 */
public interface NeighbourAirHandlerCache {
    LazyOptional<IAirHandlerMachine> getNeighbourCap();

    void setNeighbourCap(LazyOptional<IAirHandlerMachine> cap);

    NonNullConsumer<LazyOptional<IAirHandlerMachine>> getNeighbourCapInvalidationListener();

    default LazyOptional<IAirHandlerMachine> getCurrentNeighbourAirHandler(PressureTubeBlockEntity pressureTube, Direction dir) {
        BlockEntity neighborTE = pressureTube.nonNullLevel().getBlockEntity(pressureTube.getBlockPos().relative(dir));
        if (neighborTE == null) return LazyOptional.empty();

        LazyOptional<IAirHandlerMachine> cap = neighborTE.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, dir.getOpposite());
        if (!cap.isPresent()) return LazyOptional.empty();

        return cap;
    }

    default LazyOptional<IAirHandlerMachine> getCachedNeighbourAirHandler(PressureTubeBlockEntity pressureTube, Direction dir) {
        if (getNeighbourCap().isPresent()) {
            return getNeighbourCap();
        }

        LazyOptional<IAirHandlerMachine> currentCap = getCurrentNeighbourAirHandler(pressureTube, dir);
        if (getNeighbourCap() == currentCap) {
            return getNeighbourCap();
        }

        setNeighbourCap(currentCap);

        if (currentCap.isPresent()) {
            currentCap.addListener(getNeighbourCapInvalidationListener());
        }

        return currentCap;
    }
}
